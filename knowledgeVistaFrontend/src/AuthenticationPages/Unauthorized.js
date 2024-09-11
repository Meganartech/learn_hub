import React from 'react';
import { useNavigate } from 'react-router-dom';


const Unauthorized = () => {
  const navigate=useNavigate();
    return (
   
      <div className='contentbackground'>
      <div className='contentinner'>
      <div className='navigateheaders'>
      <div onClick={()=>{navigate(-2)}}><i className="fa-solid fa-arrow-left"></i></div>
      <div></div>
      <div onClick={()=>{navigate(-2)}}><i className="fa-solid fa-xmark"></i></div>
      </div>
      <div className="text-center mt-5">
      <h1 className="display-1 text-danger">401</h1>
      <h2 className="display-6">Oops! It seems you are not authorized to access this page.</h2>
      <p className="lead">Please contact the administrator for assistance..</p>
      <p>Go back To <a href="/login">Login</a></p>
     </div>
     </div>
     </div>
    );
};

export default Unauthorized;
