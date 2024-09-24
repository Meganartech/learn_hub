
import React, { useEffect, useState } from 'react'
import Swal from "sweetalert2";
import undraw_profile from "../images/profile.png"
import withReactContent from "sweetalert2-react-content";
import baseUrl from '../api/utils';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import PhoneInput, { parsePhoneNumber } from 'react-phone-number-input';
import 'react-phone-number-input/style.css';
import { isValidPhoneNumber } from 'react-phone-number-input';
import errorimg from"../images/errorimg.png"
const ProfileView = () => {
  const navigate=useNavigate();
  const token=sessionStorage.getItem("token")
  const MySwal = withReactContent(Swal);
  const [img, setImg] = useState();
  const email = sessionStorage.getItem("email");
  const [userData, setUserData] = useState({
    username: "",
    email: "",
    phone: "",
    skills: "",
    dob: "",
    
    countryCode:"",
    base64Image:null,
  });
  const [isEditing, setIsEditing] = useState(false); 
  const [defaultCountry, setDefaultCountry] = useState('IN');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [errors, setErrors] = useState({
    username: '',
    email: '',
    dob: '',
    skills:'',
    phone: '',
    fileInput:''
    
  });
 
  useEffect(() => {
    const hasErrors = Object.values(errors).some(error => !!error) ;
    const submitBtn = document.querySelector('#submitbtn');
    if (submitBtn) {
        submitBtn.disabled = hasErrors;
    }
}, [errors, userData]);
  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await axios.get(`${baseUrl}/student/users/${email}`, {
          headers: {
            Authorization: token,
          },
        });
        const userData = response.data;
        const fullPhoneNumber = `${userData.countryCode}${userData.phone}`; 
          setPhoneNumber(fullPhoneNumber)
          setUserData(userData);
        if(userData.profile !==null){
        setImg(`data:image/jpeg;base64,${userData.profile}`);
        setUserData(prevData => ({
          ...prevData,
          base64Image: `data:image/jpeg;base64,${userData.profile}`
        }));
      }
        
        
      } catch (error) {
        if(error.response && error.response.status===401)
        {
          window.location.href="/unauthorized";
        }else{
          MySwal.fire({
            title: "Error!",
            text: error.response,
            icon: "error",
            confirmButtonText: "OK",
          });
        }
      }
    };

    fetchData();
  }, [email,isEditing]);

  const handleEditClick = () => {
    setIsEditing(true);
  };
  const handlePhoneChange = (value) => {
    if (typeof value !== 'string') {
      return
    }
   
    setPhoneNumber(value);
    const phoneNumber = parsePhoneNumber(value);
    if (phoneNumber) {
      // Extract phone number without country code
      const phoneNumberWithoutCountryCode = phoneNumber.nationalNumber;
  
      setUserData((prevState) => ({
      ...prevState,
      phone:phoneNumberWithoutCountryCode ,
    }));
  }
    // Validate phone number
    if (value && isValidPhoneNumber(value)) {
      
      setErrors((prevErrors) => ({
        ...prevErrors,
        phone: '',
      }));
    } else {
      setErrors((prevErrors) => ({
        ...prevErrors,
        phone: 'Enter a valid Phone number',
      }));
    }
  };
  const fetchCountryDialingCode = async (newCountryCode) => {
    try {
      if (!newCountryCode) {
       return;
      }
     
      // Fetch country data based on the new country code (e.g., "IN", "US")
      const response = await fetch(`https://restcountries.com/v3.1/alpha/${newCountryCode}`);
      const data = await response.json();
  
     
      // Extract the dialing code from the 'idd' object in the response
      const dialingCode = data[0]?.idd?.root + (data[0]?.idd?.suffixes?.[0] || "") || "+91";
  
      // Set the country dialing code in the formData
      setUserData((prevState) => ({
        ...prevState,
        countryCode: dialingCode,
      }));

     // const countryCode = data.country_code.toUpperCase(); // Get the country code (e.g., "IN" for India, "US" for USA)
      setDefaultCountry(newCountryCode);
    } catch (error) {
      console.error("Error fetching country dialing code: ", error);
    }
  };
  const handleChange = (e) => {
    const { name, value } = e.target;
    let error = '';

    switch (name) {
      case 'username':
        error = value.length < 1 ? 'Please enter a username' : '';
        break;
        case 'skills':
          error = value.length < 1 ? 'Please enter a skill' : '';
        break;
      case 'email':
      error = /^[^\s@]+@[^\s@]+\.com$/.test(value) ? '' : 'Please enter a valid email address';
        break;
      case 'dob':
        const dobDate = new Date(value);
        const today = new Date();
        const maxDate = new Date(today.getFullYear() - 8, today.getMonth(), today.getDate()); // Min age 8 years
        const minDate = new Date(today.getFullYear() - 100, today.getMonth(), today.getDate()); // Max age 100 years
        error = dobDate <= maxDate && dobDate >= minDate ? '' : 'Please enter a valid date of birth';
        break;
     
        case 'phone':
          error = value.length < 10 ? 'Phone number must be at least 10 digits' :
         value.length > 15 ? 'Phone number cannot be longer than 15 digits' :
         /^\d+$/.test(value) ? '' : 'Please enter a valid phone number (digits only)';
  
          break;
        
        default:
          break;
    }

    setErrors(prevErrors => ({
      ...prevErrors,
      [name]: error
    }));

    setUserData(prevState => ({
        ...prevState,
        [name]: value
    }));
  };

  const convertImageToBase64 = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result);
      reader.onerror = (error) => reject(error);
    });
  };
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    
    convertImageToBase64(file)
      .then((base64Data) => {
        setUserData((prevFormData) => ({
          ...prevFormData,
          profile: file,
          base64Image: base64Data,
        }));
      })
      .catch((error) => { 
        MySwal.fire({
        title: "Error!",
        text: error ,
        icon: "error",
        confirmButtonText: "OK",
      });
      });
  };
  

  const handleSubmit = async (e) => {
      e.preventDefault();
     
      const formDataToSend = new FormData();
      formDataToSend.append("username", userData.username);
      formDataToSend.append("email", userData.email);
      formDataToSend.append("dob", userData.dob);
      formDataToSend.append("phone", userData.phone);
      formDataToSend.append("profile", userData.profile);
      formDataToSend.append("skills",userData.skills);
      
    formDataToSend.append("countryCode",userData.countryCode);
      formDataToSend.append("isActive", userData.isActive);
    
      try {
        const response = await axios.patch(`${baseUrl}/Edit/self`,formDataToSend, {
          headers: {
            Authorization: token,
          },
        });
        const data = response.data;
        if (response.status === 200 ) {
          MySwal.fire({
            title: "Updated !",
            text: "Profile Updated successfully!",
            icon: "success",
            confirmButtonText: "OK",
            
          }).then((result) => {
              if (result.isConfirmed) {
                setIsEditing(false)
                sessionStorage.removeItem("profileData")
                window.location.reload()
              }    
              
            });
          }
        
      } catch (error) {
        if(error.response && error.response.status === 400){
            setErrors(prevErrors => ({
              ...prevErrors,
              email: "This email is already registered."
            }));
        
      }else if(error.response && error.response.status === 500){
          MySwal.fire({
            title: "Server Error!",
            text: "Unexpected Error Occured",
            icon: "error",
            confirmButtonText: "OK",
          });
        }else {
            
          MySwal.fire({
            title: "Error!",
            text: error.response,
            icon: "error",
            confirmButtonText: "OK",
          });
        }
      }
  };
 



  const profileView = (
    <div className='innerFrame'>
      <h2 style={{ textDecoration: "underline" }}>Profile</h2>
      <div className='mainform'>
        <div className='profile-picture'>
          <div className='image-group'>
            <img id="preview" src={img ? img : undraw_profile} 
            onError={(e) => {
              e.target.src = errorimg; // Use the imported error image
            }}
            alt='profile' />
          </div>
        </div>
        <div className='formgroup'>
          <div className='inputgrp'>
            <label>Name</label>
            <span>:</span>
            <input   
            className='disabledbox'
                readOnly
                value={userData.username}/>
          </div>
          <div className='inputgrp'>
            <label>Email</label>
            <span>:</span>
            <input 
             className='disabledbox'
                readOnly
                value={userData.email}/>
          </div>
          <div className='inputgrp'>
            <label>Date of Birth</label>
            <span>:</span>
            <input
              className='disabledbox'
              readOnly
              value={userData.dob}/>
          </div>
          <div className='inputgrp'>
            <label>Skills</label>
            <span>:</span>
            <input
              className='disabledbox'
              readOnly
              value={userData.skills}/>
          </div>
          <div className="inputgrp "  >
                <label htmlFor="Phone">
                  {" "}
                  Phone
                  <span className="text-danger">
                    *
                  </span>
                </label>
                <span>:</span>
                <div>
                  <div>
                 
      <PhoneInput
        id="phone"
        value={phoneNumber||''}
        className='disabledbox '
        readOnly
        defaultCountry={defaultCountry}
        international
        countryCallingCodeEditable={true}
      />
    
                  </div>
                </div>
              </div>
        
        </div>
      </div>
      <div className='btngrp'>
        <button className="btn btn-primary" onClick={handleEditClick}>Edit</button>
      </div>
    </div>
  );

  const editProfileView = (
    <div className='innerFrame'>
      <h2 style={{ textDecoration: "underline" }}>Edit Profile</h2>
      <div className='mainform'>
        <div className='profile-picture'>
          <div className='image-group'>  
          {userData.base64Image ? (
                    <img
                      src={userData.base64Image }
                      onError={(e) => {
                        e.target.src = errorimg; // Use the imported error image
                      }}
                      alt="Selected Image"
                      className="profile-picture"
                    />
                  ) : (
                    <img
                      src={undraw_profile}
                      alt="Default Profile Picture"
                      className="prof"
                    />
                  )}
          </div>
          <div>
          <label htmlFor='fileInput' className='file-upload-btn'>
            Upload
          </label>
          <input
                type='file'
                name="fileInput"
                style={{display:"none"}}
                id='fileInput'
                className={`file-upload ${errors.fileInput && 'is-invalid'}`}
                accept='image/*'
                onChange={handleFileChange}
              />
              </div>
        </div>
        <div className='formgroup'>
          <div className='inputgrp'>
            <label htmlFor='Name'>Name</label>
            <span>:</span>
            <div> 
            <input
             type="text"
              id='Name'
              value={userData.username}
              onChange={handleChange}
              name="username"
              
              className={`form-control form-control-lg mt-1 ${errors.username && 'is-invalid'}`}
              placeholder="Full Name"
              autoFocus
              required
            />
            <div className="invalid-feedback">
              {errors.username}
            </div>
            </div> 
          </div>
          <div className='inputgrp'>
            <label htmlFor='email'>Email</label>
            <span>:</span>
            <div>              <input
                    type="email"
                    autoComplete="off"
                    className={`form-control form-control-lg ${errors.email && 'is-invalid'}`}
                    name="email"
                    value={userData.email}
                    onChange={handleChange}
                    placeholder="Email Address"
                    required
                  />
                  <div className="invalid-feedback">
                    {errors.email}
                  </div></div>
          </div>
          <div className='inputgrp'>
            <label htmlFor='dob'>Date of Birth</label>
            <span>:</span>
            <div>
            <input
              type="date"
                                  name="dob"
                                  className={`form-control form-control-lg ${errors.dob && 'is-invalid'}`}
                                  placeholder="Starting year"
                                  value={userData.dob}
                                  onChange={handleChange}
                                  required
                                 
            />
            <div className="invalid-feedback">
              {errors.dob}
            </div></div>
          </div>
          <div className='inputgrp'>
            <label htmlFor='skills'>Skills</label>
            <span>:</span>
            <div> <input
             type="text"
              id='skills'
              value={userData.skills }
              onChange={handleChange}
              name="skills"
              
              className={`form-control form-control-lg  ${errors.skills && 'is-invalid'}`}
              placeholder="skills"
           
              required
            />
            <div className="invalid-feedback">
              {errors.skills}
            </div></div> 
          </div>
         
          <div className="inputgrp "  >
                <label htmlFor="Phone">
                  {" "}
                  Phone
                  <span className="text-danger">
                    *
                  </span>
                </label>
                <span>:</span>
                <div>
                  <div>
                 
      <PhoneInput
        placeholder="Enter phone number"
        id="phone"
        value={phoneNumber||''}
        onChange={handlePhoneChange}
        className={`form-control form-control-lg ${
          errors.phone && "is-invalid"
        }`}
        defaultCountry={defaultCountry}
        international
        countryCallingCodeEditable={true}
        onCountryChange={fetchCountryDialingCode} 
      />
    
                    <div className="invalid-feedback">{errors.phone}</div>
                  </div>
                </div>
              </div>
        </div>
      </div>
      <div className='btngrp'>
        <button className="btn btn-primary" id='submitbtn' onClick={handleSubmit}>Save</button>
      </div>
    </div>
  );

  return (
    <div className='contentbackground'>
      <div className='contentinner'>
      <div className='navigateheaders'>
      <div onClick={()=>{navigate(-1)}}><i className="fa-solid fa-arrow-left"></i></div>
      <div></div>
      <div onClick={()=>{setIsEditing(false)}}>{isEditing ?<i className="fa-solid fa-xmark"></i>:""}</div>
      </div>
        {/* Render either profile view or edit profile view based on the state */}
        {isEditing ? editProfileView : profileView}
      </div>
    </div>
  );
}

export default ProfileView;
