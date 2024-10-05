import React, { useEffect, useState } from "react";
import "../../css/certificate.css";
import "../../css/Course.css";
import { useParams } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import Swal from "sweetalert2";
import withReactContent from "sweetalert2-react-content";
import baseUrl from "../../api/utils";
import axios from "axios";
import errorimg from "../../images/errorimg.png";
const EditLesson = () => {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const MySwal = withReactContent(Swal);

  const { courseName, Lessontitle, lessonId, courseId } = useParams();

  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadType, setUploadType] = useState("video");
  const [videodata, setvideodata] = useState({
    lessontitle: "",
    lessonDescription: "",
    fileUrl: "",
    normalurl: "",
    thumbnail: null,
    existingDocumentDetails: [],
    removedDetails:[],
    newDocumentFiles: [],
    documentPath: "",
    videoFile: null,
    base64Image: null,
  });

  const [errors, setErrors] = useState({
    lessontitle: "",
    lessonDescription: "",
    fileUrl: "",
    documentPath: "",
    thumbnail: null,
    newDocumentFiles: [],
    removedDetails:[],
    existingDocumentDetails: [],
    normalurl: "",
    videoFile: null,
    base64Image: null,
  });

  const token = sessionStorage.getItem("token");

  useEffect(() => {
    const fetchVideoData = async () => {
      try {
        const response = await axios.get(
          `${baseUrl}/lessons/getLessonsByid/${lessonId}`,
          {
            headers: {
              Authorization: token,
            },
          }
        );
        const data = await response.data;

        if (response.status === 200) {
          setvideodata(data);
          if (data.videofilename === null) {
            setUploadType("url");
            const url = data.fileUrl;
            setvideodata((videodata) => ({ ...videodata, normalurl: url }));
          }if (data.videofilename != null) {
            const filename = data.videofilename.substring(
              data.videofilename.indexOf("_") + 1
            );
            
            setSelectedFile((prevFile) => ({
              ...prevFile,
              name: filename,
            }));
          }
          
         
          if (data.thumbnail) {
            setvideodata((prevVideoData) => ({
              ...prevVideoData,
              base64Image: `data:image/jpeg;base64,${data.thumbnail}`,
              documentPath: data.documentPath,
            }));
           
          }
          const docResponse = await axios.get(
            `${baseUrl}/getDocs/${data.lessonId}`,{
              headers: {
                Authorization: token,
              },
            }
          );
         
          setvideodata((prev) => ({
            ...prev,
            existingDocumentDetails: docResponse.data,
          }));
        }
      } catch (error) {
        if (error.response) {
          if (error.response.status === 401) {
            MySwal.fire({
              title: "Un Authorized",
              text: "you are UnAuthorized to access this Page Redirecting to  back",
              icon: "error",
            }).then((result) => {
              if (result.isConfirmed) {
                navigate(-1);
              }
            });
          } else if (error.response.status === 404) {
            MySwal.fire({
              title: "Not Found",
              text: `No lessons found with lesson name ${Lessontitle}`,
              icon: "error",
            }).then((result) => {
              if (result.isConfirmed) {
                navigate(-1);
              }
            });
          }
        } else {
          MySwal.fire({
            title: "Error!",
            text: "An error occurred . Please try again later.",
            icon: "error",
            confirmButtonText: "OK",
          });
        }
      }
    };

    fetchVideoData(); // Call the async function

    // Add any dependencies if needed
  }, [lessonId, token]);

  const handleChange = (e) => {
    const { name, value } = e.target;

    let error = "";
    switch (name) {
      case "lessontitle":
        error = value.length < 1 ? "Please enter a Video Title" : "";
        setvideodata({ ...videodata, [name]: value });
        break;
      case "lessonDescription":
        error = value.length < 1 ? "Please enter a Video Description" : "";
        setvideodata({ ...videodata, [name]: value });
        break;
      case "normalurl":
        const updatedData = { ...videodata, [name]: value }; // Update normalurl

        const match = value.match(
          /^.*(?:(?:youtu\.be\/|v\/|vi\/|u\/\w\/|embed\/|shorts\/)|(?:(?:watch)?\?v(?:i)?=|\&v(?:i)?=))([^#\&\?]*).*/
        );
        let extractedId;

        if (match) {
          extractedId = match[1];
          const embedUrl = `https://www.youtube.com/embed/${extractedId}`;

          // Combine both normalurl and fileUrl updates in one setvideodata
          setvideodata({ ...updatedData, fileUrl: embedUrl });
        } else {
          setvideodata(updatedData); // Ensure the normalurl is still updated even if the match fails
          error = "Invalid YouTube URL. Please provide a valid URL.";
        }
        break;

      default:
        break;
    }
    setErrors((prevErrors) => ({
      ...prevErrors,
      [name]: error,
    }));
  };
  const handleDocChange = (e) => {
    const file = e.target.files[0];
    const allowedTypes = [
      "application/pdf",
      "application/vnd.ms-powerpoint", // For .ppt
      "application/vnd.openxmlformats-officedocument.presentationml.presentation", // For .pptx
    ];
    if (file && allowedTypes.includes(file.type)) {
      setvideodata((prevData) => ({
        ...prevData,
        newDocumentFiles: [...(prevData.newDocumentFiles || []), file], // Store the actual file, not an object
      }));
    } else {
      alert("Only PDF or PPT files are allowed!");
      // Optionally, clear the input if invalid
      e.target.value = null;
    }
   
  };

  const handleDocDrop = (e) => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    const allowedTypes = [
      "application/pdf",
      "application/vnd.ms-powerpoint", // For .ppt
      "application/vnd.openxmlformats-officedocument.presentationml.presentation", // For .pptx
    ];
    if (file && allowedTypes.includes(file.type)) {
      setvideodata((prevData) => ({
        ...prevData,
        newDocumentFiles: [...(prevData.newDocumentFiles || []), file], // Store the actual file, not an object
      }));
    } else {
      alert("Only PDF or PPT files are allowed!");
      // Optionally, clear the input if invalid
      e.target.value = null;
    }
  };
  const convertImageToBase64 = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result);
      reader.onerror = (error) => reject(error);
    });
  };
  const isSaveDisabled = () => {
    // Check if video title and description have values
    const isVideoTitleValid = videodata.lessontitle.trim().length > 0;
    const isVideoDescriptionValid =
      videodata.lessonDescription.trim().length > 0;

    // Check if either video file is selected or URL is present and valid
    let isVideoValid = false;
    if (uploadType === "video") {
      isVideoValid = selectedFile !== null;
    } else if (uploadType === "url") {
      isVideoValid =
        /^(https?:\/\/(www\.)?youtube\.com\/embed\/[\w-]+\??[\w-=&]*)$/.test(
          videodata.fileUrl
        );
    }

    // Enable save button only if all conditions are met
    return !(isVideoTitleValid && isVideoDescriptionValid && isVideoValid);
  };
  const handleFileChange = (e) => {
    const { name, value } = e.target;
    const file = e.target.files[0];

    // Update formData with the new file
    setvideodata((prevVideodata) => ({ ...prevVideodata, [name]: file }));
    if (name === "thumbnail") {
      // Convert the file to base64
      convertImageToBase64(file)
        .then((base64Data) => {
          // Set the base64 encoded image in the state
          setvideodata((prevVideodata) => ({
            ...prevVideodata,
            base64Image: base64Data,
          }));
        })
        .catch((error) => {
          console.error("Error converting image to base64:", error);
        });
    }
  };
  const handleEdit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    const formDataToSend = new FormData();
    if (videodata.lessontitle) {
      formDataToSend.append("Lessontitle", videodata.lessontitle);
    }
    if (videodata.lessonDescription) {
      formDataToSend.append("LessonDescription", videodata.lessonDescription);
    }
    if (videodata.removedDetails ) {
      videodata.removedDetails.forEach(doc => {
          formDataToSend.append("removedDetails", doc); // Append each ID
      });
      console.log("existing IDs", videodata.removedDetails.map(doc => doc.id)); // Log the IDs for verification
  }
  
    if (videodata.newDocumentFiles && videodata.newDocumentFiles.length > 0) {
      videodata.newDocumentFiles.forEach((doc, index) => {
        formDataToSend.append("newDocumentFiles", doc); // Ensure each document is appended properly
      });
    }
    console.log("formdata", formDataToSend);
    if (videodata.thumbnail) {
      formDataToSend.append("thumbnail", videodata.thumbnail);
    }

    // Send video file if available
    if (videodata.videoFile) {
      formDataToSend.append("videoFile", videodata.videoFile);
    }

    // Send file URL if available
    if (videodata.fileUrl) {
      formDataToSend.append("fileUrl", videodata.fileUrl);
    }
    for (const [key, value] of formDataToSend.entries()) {
      console.log(`${key}: ${value}`);
    }

    try {
      const response = await axios.patch(
        `${baseUrl}/lessons/edit/${videodata.lessonId}`,
        formDataToSend,
        {
          headers: {
            Authorization: token,
          },
        }
      );
      if (response.status === 200) {
        setIsSubmitting(false);
        MySwal.fire({
          title: "Updated!",
          text: " The Lesson was Updated",
          icon: "success",
          confirmButtonText: "OK",
        }).then(() => {
          window.location.href = `/lessonList/${courseName}/${courseId}`;
        });
      }
    } catch (error) {
      if (error.response && error.response.status === 401) {
        setIsSubmitting(false);
        window.location.href = "/unauthorized";
      } else if (error.response && error.response === 404) {
        setIsSubmitting(false);
        MySwal.fire({
          title: "Not Found!",
          text: " The Lesson Not Found",
          icon: "warning",
          confirmButtonText: "OK",
        });
      }
      if (error.response && error.response.status === 413) {
        setIsSubmitting(false);
        MySwal.fire({
          title: "Storage Limit Exceeded",
          text: error.response.data,
          icon: "warning",
        });
      } else {
        setIsSubmitting(false);
        MySwal.fire({
          title: "error!",
          text: "Some Unexpected Error occured . Please try again later.",
          icon: "error",
          confirmButtonText: "OK",
        });
      }
    }
  };
  const handleRemoveExisDoc = (indexToRemove) => {
    // Get the document object to be removed
    const docToRemove = videodata.existingDocumentDetails[indexToRemove];
  
    // Get the ID of the document to be removed
    const docToRemoveId = docToRemove.id; // Access the id property
  
    // Update the removedDetails state
    const updatedRemovedDetails = [...(videodata.removedDetails || []), docToRemoveId];
  
    // Filter out the document from existingDocumentDetails
    const updatedDocs = videodata.existingDocumentDetails.filter(
      (doc, index) => index !== indexToRemove
    );
  
    // Update the state to remove the file and add the removed ID
    setvideodata({ 
      ...videodata, 
      existingDocumentDetails: updatedDocs,
      removedDetails: updatedRemovedDetails // Add the removed ID here
    });
  };
  
  const handleRemoveDoc = (indexToRemove) => {
    const updatedDocs = videodata.newDocumentFiles.filter(
      (doc, index) => index !== indexToRemove
    );
    // Update the state to remove the file
    setvideodata({ ...videodata, newDocumentFiles: updatedDocs });
  };

  const handleDrop = (e) => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    const maxSize = 1024 * 1024 * 1024;
      if (file && file.size > maxSize) {
      alert("File size exceeds 1 GB limit.");
    } else {
      if (file && file.type.includes("video")) {
        // Set the selected file name in state
        setSelectedFile((prev) => ({
          ...prev,
          name: file.name, // Set the file name in selectedFile
        }));
  
        // Update the videodata state with the file
        setvideodata((prevData) => ({
          ...prevData,
          videoFile: file, 
          fileUrl: "", // Clear file URL if necessary
        }));
      } else {
        alert("Please select a video file.");
      }
    }
  };
  const handleFileInputChange = (e) => {
    const file = e.target.files[0];
    const maxSize = 1024 * 1024 * 1024; // 1 GB in bytes
  
    if (file && file.size > maxSize) {
      alert("File size exceeds 1 GB limit.");
    } else {
      if (file && file.type.includes("video")) {
        // Set the selected file name in state
        setSelectedFile((prev) => ({
          ...prev,
          name: file.name, // Set the file name in selectedFile
        }));
  
        // Update the videodata state with the file
        setvideodata((prevData) => ({
          ...prevData,
          videoFile: file, 
          fileUrl: "", // Clear file URL if necessary
        }));
      } else {
        alert("Please select a video file.");
      }
    }
  };
  
  
  const handleUploadTypeChange = (e) => {
    setUploadType(e.target.value);
  };
  const handleDragOver = (e) => {
    e.preventDefault();
  };
  return (
    <div className="contentbackground">
      <div className="contentinner">
        <div className="navigateheaders">
          <div
            onClick={() => {
              navigate(-1);
            }}
          >
            <i className="fa-solid fa-arrow-left"></i>
          </div>
          <div></div>
          <div
            onClick={() => {
              navigate("/dashboard/course");
            }}
          >
            <i className="fa-solid fa-xmark"></i>
          </div>
        </div>
        <form onSubmit={handleEdit}>
          {isSubmitting && <div className="loading-spinner"></div>}
          <div className="divider">
            <h2 style={{ textDecoration: "underline" }}>
              Edit Lesson {Lessontitle}{" "}
            </h2>
            <div className="innerdivider">
              <div className="textinputs">
                <div className="grp">
                  <label>Video Title</label>
                  <div>
                    <input
                      type="text"
                      placeholder="Video Title"
                      name="lessontitle"
                      value={videodata.lessontitle}
                      onChange={handleChange}
                      className={`form-control .form-control-sm  mt-1 ${
                        errors.lessontitle && "is-invalid"
                      }`}
                      disabled={isSubmitting}
                    />
                    <div className="invalid-feedback">{errors.lessontitle}</div>
                  </div>
                </div>
                <div className="grp">
                  <label>Description</label>
                  <div>
                    <textarea
                      name="lessonDescription"
                      value={videodata.lessonDescription}
                      className={`form-control .form-control-sm  mt-1 ${
                        errors.lessonDescription && "is-invalid"
                      }`}
                      placeholder="Video Description"
                      rows={4}
                      disabled={isSubmitting}
                      onChange={handleChange}
                    ></textarea>
                    <div className="invalid-feedback">
                      {errors.lessonDescription}
                    </div>
                  </div>
                </div>
                <div className="thumb">
                  <label id="must">Thumbnail</label>
                  <label
                    htmlFor="thumbnail"
                    id="must"
                    style={{ width: "100px", height: "40px" }}
                    className="file-upload-btn"
                  >
                    Upload
                  </label>
                  <input
                    onChange={handleFileChange}
                    disabled={isSubmitting}
                    name="thumbnail"
                    type="file"
                    id="thumbnail"
                    className="file-upload"
                    accept="image/*"
                  />
                  {videodata.base64Image && (
                    <img
                      src={videodata.base64Image}
                      onError={(e) => {
                        e.target.src = errorimg; // Use the imported error image
                      }}
                      alt="Selected "
                      style={{ width: "100px", height: "100px" }}
                    />
                  )}
                </div>
                <div className="grp">
                  <label>Attachments</label>

                  <div
                    className="dropzone"
                    onDrop={handleDocDrop}
                    onDragOver={handleDragOver}
                  >
                    <p>Drag and drop </p>

                    <p>or</p>
                  <label
                    htmlFor="existingDocumentDetails"
                    id="must"
                    
                    style={{ margin: "auto", width: "200px" }}
                      className="file-upload-btn"
                  >
                    Upload
                  </label>
                  <p>Allowed Files .pdf |.ppt |.pptx</p>
                  <input
                    type="file"
                    accept="application/pdf,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation"
                      
                    name="existingDocumentDetails"
                    id="existingDocumentDetails"
                    className={`file-upload ${
                      errors.existingDocumentDetails && "is-invalid"
                    }`}
                    onChange={handleDocChange}
                  />
                  <div>
                    {videodata.existingDocumentDetails &&
                      videodata.existingDocumentDetails.length > 0 && (
                        <ul>
                          {videodata.existingDocumentDetails.map(
                            (doc, index) => (
                              <li
                                key={index}
                               
                              >
                                <div 
                                className="doclink2"
                                onClick={() => {
                                  navigate("/viewDocument", {
                                    state: { doc: doc , lessonId: lessonId,},
                                  });
                                }}> {doc.documentName &&(
                                  <>
                                    {doc.documentName}{" "}
                                    {doc.documentName.endsWith(".pdf") && (
                                      <i className="fa-regular fa-file-pdf"></i>
                                    )}
                                    {(doc.documentName.endsWith(".ppt") ||
                                      doc.documentName.endsWith(".pptx")) && (
                                      <i className="fa-regular fa-file-powerpoint"></i>
                                    )}
                                     
                                  </>
                                )}</div><i
                                className="fa-regular fa-trash-can"
                                style={{
                                  marginLeft: "10px",
                                  cursor: "pointer",
                                }}
                                onClick={() => handleRemoveExisDoc(index)}
                              ></i>
                               
                              </li> // Display document name
                            )
                          )}
                        </ul>
                      )}
                    {videodata.newDocumentFiles &&
                      videodata.newDocumentFiles.length > 0 && (
                        <ul>
                          {videodata.newDocumentFiles.map((doc, index) => (
                            <li key={index} className="doclink2" >
                              {doc.name &&(
                                <>
                                  {doc.name}{" "}
                                  {doc.name.endsWith(".pdf") && (
                                    <i className="fa-regular fa-file-pdf"></i>
                                  )}
                                  {(doc.name.endsWith(".ppt") ||
                                    doc.name.endsWith(".pptx")) && (
                                    <i className="fa-regular fa-file-powerpoint"></i>
                                  )}
                                </>
                              )}
                               <i
                                className="fa-regular fa-trash-can"
                                style={{
                                  marginLeft: "10px",
                                  cursor: "pointer",
                                }}
                                onClick={() => handleRemoveDoc(index)}
                              ></i>
                            </li> // Display document name
                          ))}
                        </ul>
                      )}
                  </div>
                </div>
                </div>
              </div>

              <div className="videoinputs">
                <div>
                  <input
                    type="radio"
                    name="uploadType"
                    id="video"
                    value="video"
                    checked={uploadType === "video"}
                    onChange={handleUploadTypeChange}
                    disabled={isSubmitting}
                  />
                  <label htmlFor="video" style={{ marginLeft: "20px" }}>
                    Video
                  </label>
                </div>
                <div>
                  <input
                    type="radio"
                    name="uploadType"
                    id="url"
                    value="url"
                    checked={uploadType === "url"}
                    onChange={handleUploadTypeChange}
                    disabled={isSubmitting}
                  />
                  <label htmlFor="url" style={{ marginLeft: "20px" }}>
                    Youtube Url
                  </label>
                </div>

                {uploadType === "url" ? (
                  <>
                    <div>
                      <input
                        type="text"
                        name="normalurl"
                        placeholder="Enter Youtube URL"
                        value={videodata.normalurl}
                        onChange={handleChange}
                        className={`form-control .form-control-sm  mt-1 urlinput ${
                          errors.normalurl && "is-invalid"
                        }`}
                        disabled={isSubmitting}
                      />
                      <div className="invalid-feedback">{errors.normalurl}</div>
                      {videodata.fileUrl && (
                        <>
                          <div>
                            <input
                              type="text"
                              name="fileUrl"
                              value={videodata.fileUrl}
                              onChange={handleChange}
                              className="disabledbox mt-2"
                              style={{ height: "20px" }}
                              readOnly
                              disabled={isSubmitting}
                              data-toggle="tooltip"
                              data-placement="top"
                              title="Embeded Url"
                            />
                          </div>
                          <div>
                            {videodata.fileUrl && (
                              <iframe
                                style={{ marginTop: "10px" }}
                                src={videodata.fileUrl}
                              ></iframe>
                            )}
                          </div>
                        </>
                      )}
                    </div>
                  </>
                ) : (
                  <div
                    className="dropzone"
                    onDrop={handleDrop}
                    onDragOver={handleDragOver}
                  >
                    <p>Drag and drop </p>
                    <p>or</p>
                    <label
                      htmlFor="videoupload"
                      style={{ width: "200px" }}
                      className="file-upload-btn"
                    >
                      {" "}
                      upload Video
                    </label>
                    <input
                      type="file"
                      className="file-upload"
                      id="videoupload"
                      name="file"
                      accept="video/*"
                      onChange={handleFileInputChange}
                      disabled={isSubmitting}
                    />
                    {selectedFile && selectedFile.name && (
                      <p>
                     {selectedFile.name} 
                      </p>
                    )}
                  </div>
                )}
              </div>
            </div>

            <div className="cornerbtn ">
              <button
                className="btn btn-primary"
                onClick={() => {
                  navigate(-1);
                }}
              >
                Cancel
              </button>

              <button
                className="btn btn-primary"
                disabled={isSaveDisabled()}
                type="submit"
              >
                Save
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditLesson;
