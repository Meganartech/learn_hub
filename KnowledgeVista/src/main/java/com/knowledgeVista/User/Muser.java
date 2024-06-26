package com.knowledgeVista.User;
import java.time.LocalDate;
import java.util.List;
import com.knowledgeVista.Course.CourseDetail;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Getter@Setter@NoArgsConstructor
public class Muser {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long userId;
	    private String username;
	    private String psw;
	    @Column(unique = true)
	    private String email;
	    private LocalDate dob;
	    private String phone;
	    private String skills;
	    @Lob
	    @Column(name="profile" ,length=1000000)
	   
	    private byte[] profile;
	    @ManyToOne
	    @JoinColumn(name = "roleId")
	    private MuserRoles role;
	    @Column( columnDefinition = "varchar(10)")
	    private String countryCode;
	    @ManyToMany
	    @JoinTable(
	        name = "user_allotedCourse",
	        joinColumns = @JoinColumn(name = "user_id"),
	        inverseJoinColumns = @JoinColumn(name = "course_id")
	        )
	    private List<CourseDetail> allotedCourses;

	    
	    @ManyToMany
	    @JoinTable(
	        name = "user_course",
	        joinColumns = @JoinColumn(name = "user_id"),
	        inverseJoinColumns = @JoinColumn(name = "course_id")
	    )
	    private List<CourseDetail> courses;

	    private Boolean isActive;
}
