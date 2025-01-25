import React, { useEffect, useState } from "react";
import Swal from "sweetalert2";
import withReactContent from "sweetalert2-react-content";
import baseUrl from "../../api/utils";
import errorimg from "../../images/errorimg.png";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import SelectPaymentGateway from "../Payments/SelectPaymentGateway";
const CourseView = ({ filteredCourses }) => {
  const MySwal = withReactContent(Swal);
  const userId = sessionStorage.getItem("userid");
  const [submitting, setsubmitting] = useState(false);
  const token = sessionStorage.getItem("token");
  const navigate =useNavigate();
  const Currency=sessionStorage.getItem("Currency");
  const[openselectgateway,setopenselectgateway]=useState(false)
  const[orderData,setorderData]=useState({
    userId:"",
    courseId:"",
    amount:"" ,
    courseAmount:"",
    coursename:"",
    installment:"",
    paytype:"",
    url:""
})
  // useEffect(() => {
  //   const pendingPayment = JSON.parse(sessionStorage.getItem("pendingPayment"));

  //   if (pendingPayment) {
  //     const { courseId, paytype } = pendingPayment;

  //     // Clear pending payment data from localStorage
  //     sessionStorage.removeItem("pendingPayment");
  //     const userId = sessionStorage.getItem("userid");
  //     // Resume the payment process
  //     handlepaytype(courseId, userId, paytype);
  //   }
  // }, []);

  // const handlepaytype = (courseId, userId, paytype) => {
  //   let url = "";
  //   if (paytype === "FULL") {
  //     url = "/Full/getOrderSummary";
  //     FetchOrderSummary(courseId, userId, url);
  //   } else {
  //     MySwal.fire({
  //       icon: "question",
  //       title: "Payment Type?",
  //       text: "Want To Pay the Amount Partially or Fully? ",
  //       showDenyButton: true,
  //       showCancelButton: true,
  //       confirmButtonColor: "#4e73df",
  //       denyButtonColor: "#4e73df",
  //       confirmButtonText: `Pay Fully `,
  //       denyButtonText: `Pay in  Part`,
  //     }).then((result) => {
  //       if (result.isConfirmed) {
  //         url = "/Full/getOrderSummary";
  //         FetchOrderSummary(courseId, userId, url);
  //       } else if (result.isDenied) {
  //         url = "/Part/getOrderSummary";

  //         FetchOrderSummary(courseId, userId, url);
  //       }
  //     });
  //   }
  // };
//   const FetchOrderSummary=async(courseId, userId, url) =>{
//     try {
//           setsubmitting(true);
//           const data = JSON.stringify({
//             courseId: courseId,
//             userId: userId,
//           });
    
//           const response = await axios.post(`${baseUrl}${url}`, data, {
//             headers: {
//               Authorization: token,
//               "Content-Type": "application/json",
//             },
//           });
//           setsubmitting(false);

// setorderData(response.data)
// setopenselectgateway(true)
//         }catch(error){
//           setsubmitting(false);
//           setopenselectgateway(false);
//               if(error.response && error.response.status===400){
             
//               MySwal.fire({
//                 icon: "error",
//                 title: "Error creating order:",
//                 text: error.response.data ? error.response.data : "error occured",
//               });
//             }else{
//               throw error
//             }
//         }
//   }
 
 
  const handleClick = async (event, id, amount, url) => {
    event.preventDefault();
    if (amount === 0) {
      navigate(url)
    } else {
      try {
        const formdata = JSON.stringify({ courseId: id });
        const response = await axios.post(
          `${baseUrl}/CheckAccess/match`,
          formdata,
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: token,
            },
          }
        );

        if (response.status === 200) {
          const message = response.data;
          navigate(message);
        }
      } catch (error) {
        if (error.response.status === 401) {
          MySwal.fire({
            icon: "error",
            title: "Oops...",
            text: "cannot Access Course ",
          });
        } else {
          // MySwal.fire({
          //   icon: "error",
          //   title: "Not Found",
          //   text: error,
          // });
          throw error
        }
      }
    }
  };

  return (
    <>
      {submitting && (
        <div className="outerspinner active">
          <div className="spinner"></div>
        </div>
      )}
      {openselectgateway && (
        <SelectPaymentGateway orderData={orderData} setorderData={setorderData} setopenselectgateway={setopenselectgateway}/>
      )}
      <div className="page-header"></div>
    
      {filteredCourses.length > 0 ? (
        <div className="row">
        
          {filteredCourses
            .slice()
            .reverse()
            .map((item) => (
              <div className="col-md-6 col-xl-3 course" key={item.courseId}>
                <div className="card mb-3 ">
                  <img
                   style={{ cursor: "pointer" }}
                   onClick={(e) => {
                     handleClick(
                       e,
                       item.courseId,
                       item.amount,
                       item.courseUrl
                     );
                   }}
                   title={`${item.courseName} image`}
                    className="img-fluid card-img-top"
                    src={`data:image/jpeg;base64,${item.courseImage}`}
                    onError={(e) => {
                      e.target.src = errorimg; // Use the imported error image
                    }}
                    alt="Course"
                  />
                  <div className="card-body">
                    <h5
                      className="courseName"
                      title={item.courseName}
                      style={{ cursor: "pointer" }}
                      // onClick={(e) => {
                      //   handleClick(
                      //     e,
                      //     item.courseId,
                      //     item.amount,
                      //     item.courseUrl
                      //   );
                      // }}
                      onClick={()=>{
                        navigate(`/batch/viewall/${item.courseId}`)
                      }}
                    >
                      {item.courseName}
                    </h5>
                   <p title={item.courseDescription} className="courseDescription">
                    {item.courseDescription}
                    </p>
                    <div>
                      {item.amount === 0 ? (
                        <a
                          title="Enroll For Free"
                          onClick={(e)=>{ e.preventDefault();navigate(item.courseUrl)}}
                          className="btn btn-sm btn-outline-success w-100"
                        >
                          Enroll for Free
                        </a>
                      ) : (
                        <div
                          className="amountGrid"
                        >
                          <div className="amt">
                             <i className={Currency === "INR" ? "fa-solid fa-indian-rupee-sign pr-1" : "fa-solid fa-dollar-sign pr-1"}></i>
                              <span title={item.amount} >
                              {item.amount}
                            </span>
                          </div>
                          <button
                            className=" btn btn-sm btn-outline-primary"
                            // onClick={() =>
                            //   handlepaytype(item.courseId, userId, item.paytype)
                            // }
                            onClick={()=>{
                              navigate(`/batch/viewall/${item.courseId}`)
                            }}
                            title="Enroll Now"
                          >
                            Enroll Now
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
                 </div>
      ) : (
        <div >
        <h1 className="text-light ">No Course Found </h1>
        </div>
      )}
    
    </>
  );
};

export default CourseView;