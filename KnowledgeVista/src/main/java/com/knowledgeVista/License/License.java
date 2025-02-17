package com.knowledgeVista.License;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name="license")
public class License {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;
	
    @Column(name="company_name")
    private String company_name ;
    
    @Column(name="product_name")
    private String product_name ;
    
    @Column(name="key_value")
    private String key ;
    
    @Column(name="key_value2")
    private String key2 ;
    
    @Column(name="course")
    private String course ;
    
    @Column(name="students")
    private String students ;
    
    @Column(name="trainer")
    private String trainer ;
    
    @Column(name="type")
    private String type ;
    
    @Column(name="start_date")
    private Date start_date ;
    
    @Column(name="end_date")
    private Date end_date ;
    
    @Column(name="file_name")
    private String filename ;
    
	@Column(name="institution", unique=true)
    private String institution;
	
	@Column(name="storagesize")
    private Long storagesize;
    
   
	@Transient
	 private MultipartFile LicenseFile;
    
	 public Long getStoragesize() {
			return storagesize;
		}

		public void setStoragesize(Long storagesize) {
			this.storagesize = storagesize;
		}
		
    public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	
	public MultipartFile getLicenseFile() {
		return LicenseFile;
	}

	public void setLicenseFile(MultipartFile licenseFile) {
		LicenseFile = licenseFile;
	}
	
	public String getStudents() {
		return students;
	}

	public void setStudents(String students) {
		this.students = students;
	}

	public String getTrainer() {
		return trainer;
	}

	public void setTrainer(String trainer) {
		this.trainer = trainer;
	}


	public License(long id, String company_name, String product_name, String key, String key2, String course,
			String students, String trainer, String type, Date start_date, Date end_date, String filename,
			String institution, Long storagesize, MultipartFile licenseFile) {
		super();
		this.id = id;
		this.company_name = company_name;
		this.product_name = product_name;
		this.key = key;
		this.key2 = key2;
		this.course = course;
		this.students = students;
		this.trainer = trainer;
		this.type = type;
		this.start_date = start_date;
		this.end_date = end_date;
		this.filename = filename;
		this.institution = institution;
		this.storagesize = storagesize;
		LicenseFile = licenseFile;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCompany_name() {
		return company_name;
	}

	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}

	public String getProduct_name() {
		return product_name;
	}

	public void setProduct_name(String product_name) {
		this.product_name = product_name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	
	public String getKey2() {
		return key2;
	}

	public void setKey2(String key2) {
		this.key2 = key2;
	}

	public Date getStart_date() {
		return start_date;
	}

	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}

	public Date getEnd_date() {
		return end_date;
	}

	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}

	public License() {
		super();
	}

	@Override
	public String toString() {
		return "License [id=" + id + ", company_name=" + company_name + ", product_name=" + product_name + ", key="
				+ key + ", key2=" + key2 + ", course=" + course + ", students=" + students + ", trainer=" + trainer
				+ ", type=" + type + ", start_date=" + start_date + ", end_date=" + end_date + ", filename=" + filename
				+ ", institution=" + institution + ", storagesize=" + storagesize + ", LicenseFile=" + LicenseFile
				+ "]";
	}
	

	

	
	
    

}
