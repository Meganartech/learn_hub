import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import { useState ,useEffect } from "react";
import "./css/StudentRegister.css"
import StudentRegister from "./Student/StudentRegister";
import ForgetPassword from "./AuthenticationPages/forgetpassword";
import Login from "./AuthenticationPages/login";
import ResetPassword from "./AuthenticationPages/ResetPassword";
import React from "react";
import PrivateRoute from "./AuthenticationPages/PrivateRoute";
import Missing from "./AuthenticationPages/Missing";
import Unauthorized from "./AuthenticationPages/Unauthorized";
import AttenTest from "./course/Test/AttenTest";
import CertificateInputs from "./certificate/CertificateInputs";
import Template from "./certificate/Template";
import EditCourse from "./course/Update/EditCourse";
import CourseView from "./course/Components/CourseView";
import Swal from "sweetalert2";
import withReactContent from "sweetalert2-react-content";
import CourseDetails from "./course/CourseDetails.js";
import Layout from "./Common Components/Layout.js";
import CreateTest from "./course/Test/CreateTest";
import TestList from "./course/Test/TestList";
import ViewStudentList from "./Student/ViewStudentList";
import AssignCourse from "./Student/AssignCourse.js";
import Mycourse from "./Student/Mycourse";
import MyCertificateList from "./certificate/MyCertificateList";
import AddTrainer from "./Trainer/AddTrainer";
import AddStudent from "./Student/AddStudent";
import ViewTrainerList from "./Trainer/ViewTrainerList";
import UploadVideo from "./course/Components/UploadVideo";
import CourseCreation from "./course/Components/CourseCreation";
import ViewVideo from "./course/Components/ViewVideo";
import LessonList from "./course/Components/LessonList";
//import Dashboard from "./course/Components/Dashboard.js";
import License from "./AuthenticationPages/License.js";
import Razorpay_Settings from "./AuthenticationPages/Razorpay_Settings.js";
//import Feedback from "./AuthenticationPages/Feedback.js";
import TrainerProfile from "./Trainer/TrainerProfile.js";
import StudentProfile from "./Student/StudentProfile.js";
import MyAssignedcourses from "./Trainer/MyAssignedcourses.js";
import EditLesson from "./course/Update/EditLesson.js";
import AssignCourseTRAINER from "./Trainer/AssignCourseTRAINER.js";
import CreateCourseTrainer from "./Trainer/CreateCourseTrainer.js";
import EditStudent from "./Student/EditStudent.js";
import EditTrainer from "./Trainer/EditTrainer.js";
import ProfileView from "./Common Components/ProfileView.js";
import VideoCheck from "./VideoCheck.js";
import CustomViewvideo from "./course/Components/CustomViewvideo.js";
import EditQuestion from "./course/Test/EditQuestion.js";



function App() {
  
  const isAuthenticated = sessionStorage.getItem('token') !== null;
  const MySwal = withReactContent(Swal);
  const [isToggled, setIsToggled] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [course, setCourse] = useState([{
    courseId:"",
    courseName:"",
    courseUrl:"",
    courseDescription:"",
    courseCategory:"",
    amount:"",
    courseImage:"",
    Duration:"",
    Noofseats:""
  }]);
  
  const handleSearchChange = (e) => {
    setSearchQuery(e.target.value);
  };
  const filteredCourses = course.filter((item) => {
    const name = item.courseName ? item.courseName.toLowerCase() : "";
    return name.includes(searchQuery.toLowerCase());
  });
  
  useEffect(() => {
    const fetchItems = async () => {
      try {
        const response = await fetch("http://localhost:8080/course/viewAll");

        const data = await response.json();

        setCourse(data);
   
      } catch (error) {
       console.error(error);
      }
    };
  if(isAuthenticated){
    fetchItems();}
  }, []);

  return (
    <Router>
      <div className="App ">
        <Routes>
                  <Route element={<Layout  
                  isToggled={isToggled}
                  setIsToggled={setIsToggled}
                  searchQuery={searchQuery}
                  handleSearchChange={handleSearchChange}
                  setSearchQuery={setSearchQuery}/>}>
                    {/* <Route path="/admin/dashboard" element={<PrivateRoute onlyadmin={true} authenticationRequired={true} authorizationRequired={true}><Dashboard/></PrivateRoute>}/> */}
                      <Route path="/lessonList/:courseName/:courseId" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><LessonList/></PrivateRoute>}/> 
                      <Route path="/edit/:courseName/:courseId/:Lessontitle/:lessonId" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><EditLesson/></PrivateRoute>}/>
                      <Route path="/courses/:courseName/:courseId/" element={<PrivateRoute authenticationRequired={true} ><ViewVideo/></PrivateRoute>}/>                     
                      <Route path="/courses/:courseName/:courseId/:current" element={<PrivateRoute authenticationRequired={true} ><CustomViewvideo/></PrivateRoute>}/>                 
                      <Route path="/course/Addlesson/:courseName/:courseId" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><UploadVideo/></PrivateRoute>}/>
                      <Route path="/course/addcourse" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><CourseCreation/></PrivateRoute>}/>
                      <Route path="/course/Trainer/addcourse" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true} onlytrainer={true}><CreateCourseTrainer/></PrivateRoute>}/>
                      <Route path="/addTrainer" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><AddTrainer/></PrivateRoute>}/>
                      <Route path="/addStudent" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><AddStudent/></PrivateRoute>}/>
                      <Route path="/mycourses" element={<PrivateRoute authenticationRequired={true} ><Mycourse/></PrivateRoute>}/>
                      <Route path="/dashboard/course" element={<PrivateRoute authenticationRequired={true}><CourseView  filteredCourses={filteredCourses} /></PrivateRoute>} />
                      <Route path="/course/admin/edit" element={<PrivateRoute onlyadmin={true} authenticationRequired={true} authorizationRequired={true}><EditCourse  filteredCourses={filteredCourses} /></PrivateRoute>} />
                      <Route path="/course/AddTest/:courseId" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><CreateTest /></PrivateRoute>} />
                      <Route path="/test/start/:courseName/:courseId" element={<PrivateRoute onlyuser={true} authenticationRequired={true}><AttenTest/></PrivateRoute>}/>
                      <Route path="/test/Edit/:questionId" element={<PrivateRoute authorizationRequired={true} authenticationRequired={true}><EditQuestion/></PrivateRoute>}/>
                     <Route path="/course/edit/:courseId" element={<PrivateRoute authenticationRequired={true}><CourseDetails/></PrivateRoute>}/>
                      <Route path="/course/testlist/:courseId" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><TestList /></PrivateRoute>} />
                      <Route path="/course/dashboard/profile" element={<PrivateRoute authenticationRequired={true}><ProfileView/></PrivateRoute>}/>
                      <Route path="/certificate" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><CertificateInputs/></PrivateRoute>}/>
                      <Route path="/MyCertificateList" element={<PrivateRoute authenticationRequired={true}><MyCertificateList /></PrivateRoute>}/>
                      <Route path="/template/:activityId" element={<PrivateRoute authenticationRequired={true}><Template/></PrivateRoute>}/>
                      <Route path="/settings/payment" element={<PrivateRoute onlyadmin={true} authenticationRequired={true} authorizationRequired={true}><Razorpay_Settings/></PrivateRoute>}/>
                      <Route path="/assignCourse/Student/:userId" element={<PrivateRoute  authenticationRequired={true} authorizationRequired={true}><AssignCourse /></PrivateRoute>} />
                      <Route path="/assignCourse/Trainer/:userId" element={<PrivateRoute onlyadmin={true} authenticationRequired={true} authorizationRequired={true}><AssignCourseTRAINER /></PrivateRoute>} />
                      <Route path="/view/Students" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><ViewStudentList/></PrivateRoute>}/>
                      <Route path="/view/Trainer" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><ViewTrainerList/></PrivateRoute>}/>
                      <Route path="/certificate" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><CertificateInputs/></PrivateRoute> }/>
                      <Route path="/view/Trainer/profile/:traineremail" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><TrainerProfile/></PrivateRoute>}/>
                      <Route path="/view/Student/profile/:studentemail" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><StudentProfile/></PrivateRoute>}/>
                      <Route path="/student/edit/:email" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><EditStudent/></PrivateRoute>}/>
                      <Route path="/trainer/edit/:email" element={<PrivateRoute authenticationRequired={true} authorizationRequired={true}><EditTrainer/></PrivateRoute>}/>
                      <Route path="/AssignedCourses" element={<PrivateRoute onlytrainer={true} authenticationRequired={true} ><MyAssignedcourses/></PrivateRoute>}/>
                  </Route> 

           <Route path="/License" element={<License/>}/>
           {/* <Route path="/Feedback" element={<Feedback/>}/> */}
          <Route path="/" element={<StudentRegister/>}/>
          <Route path="/unauthorized" element={<Unauthorized/>}/>
          <Route path="/forgot-password" element={<ForgetPassword />} />
          <Route path="/login" element={<Login />} />
          <Route path="/resetpassword" element={<ResetPassword />}></Route>
          <Route path="*" element={<Missing/>}/>
          <Route path="/videocheck" element={<VideoCheck/>}/>
                  </Routes>
      </div>
    </Router>  );
}

export default App;
