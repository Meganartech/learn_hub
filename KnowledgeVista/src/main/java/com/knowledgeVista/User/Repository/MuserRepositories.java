package com.knowledgeVista.User.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.knowledgeVista.Batch.SearchDto;
import com.knowledgeVista.Course.CourseDetailDto;
import com.knowledgeVista.Migration.MuserMigrationDto;
import com.knowledgeVista.User.Muser;
import com.knowledgeVista.User.MuserDto;
import com.knowledgeVista.User.MuserProfileDTO;
import com.knowledgeVista.User.MuserRequiredDto;
import com.knowledgeVista.User.UserStats;


@Repository
public interface MuserRepositories extends JpaRepository<Muser,Long> {


	@Query("SELECT u FROM Muser u WHERE u.email = ?1")
  Optional<Muser> findByEmail(String email);
	
	
	
	@Query("SELECT u.userId FROM Muser u WHERE u.email = ?1")
	Long  findidByEmail(String email);
	
	@Query("SELECT u.institutionName FROM Muser u WHERE u.role.roleName = :rolename")
	String getInstitution(String rolename);
	@Query("SELECT u.email FROM Muser u WHERE u.userId=?1")
   String FindEmailByuserId(Long userId);
	@Query("SELECT u.institutionName FROM Muser u WHERE u.email = ?1")
  String findinstitutionByEmail(String email);
	 @Query("SELECT new com.knowledgeVista.User.MuserDto(u.userId, u.username, u.email, u.phone, u.isActive, u.dob, u.skills, u.institutionName) " +
	           "FROM Muser u " +
	           "WHERE u.email = :email")
	    Optional<MuserDto> findDetailsByEmailforSysadmin(@Param("email") String email);
	
	  @Query("SELECT new com.knowledgeVista.User.MuserDto(u.userId, u.username, u.email, u.phone, u.isActive, u.dob, u.skills, u.institutionName) " +
	           "FROM Muser u " +
	           "WHERE u.email = :email AND u.institutionName = :institutionName")
	    Optional<MuserDto> findDetailsByEmailAndInstitution(@Param("email") String email,
	                                                        @Param("institutionName") String institutionName);
	  
	  @Query("SELECT new com.knowledgeVista.User.MuserRequiredDto(u.userId, u.username, u.email, u.phone, u.isActive, u.dob, u.skills, u.institutionName,u.profile, u.countryCode) " +
	           "FROM Muser u " +
	           "WHERE u.email = :email AND u.institutionName = :institutionName")
	    Optional<MuserRequiredDto> findDetailandProfileByEmailAndInstitution(@Param("email") String email,
	                                                        @Param("institutionName") String institutionName);
	  
@Query("SELECT new com.knowledgeVista.User.MuserProfileDTO(u.profile, u.countryCode, u.role.roleName ) " +
	           "FROM Muser u " +
	           "JOIN u.role r " +
	           "WHERE u.email = :email AND u.institutionName = :institutionName")
	    Optional<MuserProfileDTO> findProfileAndCountryCodeAndRoleByEmailAndInstitutionName(
	        @Param("email") String email,
	        @Param("institutionName") String institutionName
	    );

@Query("SELECT new com.knowledgeVista.User.MuserProfileDTO(u.profile, u.countryCode, u.role.roleName ,u.lastactive) " +
        "FROM Muser u " +
        "JOIN u.role r " +
        "WHERE u.email = :email ")
 Optional<MuserProfileDTO> findProfileAndCountryCodeAndRoleByEmail(
     @Param("email") String email
 );

@Query("SELECT MAX(m.lastactive) FROM Muser m WHERE m.institutionName = :institutionName")
LocalDateTime findLatestLastActiveByInstitution(@Param("institutionName") String institutionName);


@Query("SELECT new com.knowledgeVista.Course.CourseDetailDto(c.courseId, c.courseName, c.courseUrl, c.courseDescription, c.courseCategory, c.amount, c.courseImage, c.paytype, c.Duration, c.institutionName, c.Noofseats) " +
        "FROM Muser u " +
        "JOIN u.allotedCourses c " +
        "WHERE u.email = :email")
 List<CourseDetailDto> findAllotedCoursesByEmail(@Param("email") String email);

@Query("SELECT new com.knowledgeVista.Course.CourseDetailDto(c.courseId, c.courseName, c.courseUrl, c.courseDescription, c.courseCategory, c.amount, c.courseImage, c.paytype, c.Duration, c.institutionName, c.Noofseats) " +
        "FROM Muser u " +
        "JOIN u.courses c " +
        "WHERE u.email = :email")
 List<CourseDetailDto> findStudentAssignedCoursesByEmail(@Param("email") String email);
	
	@Query("SELECT u FROM Muser u WHERE u.email = ?1 AND u.institutionName = ?2")
  Optional<Muser> findByEmailandInstitutionName(String email, String institutionName);
	
	@Query("SELECT new com.knowledgeVista.User.MuserRequiredDto(u.userId, u.username, u.email, u.profile) " +
	          "FROM Muser u WHERE u.userId = ?1 AND u.institutionName = ?2")
	  Optional<MuserRequiredDto> findByuserIdandInstitutionName(Long userId, String institutionName);

	
	@Query("SELECT u from Muser u WHERE u.institutionName = ?1")
	Optional<Muser>findByInstitutionName(String institutionName);
	
	@Query("SELECT u from Muser u WHERE u.institutionName = ?1")
	List<Muser>findByInstitutionNameall(String institutionName);
	
	   
	   @Query("SELECT u FROM Muser u WHERE u.role.roleName = :rolename AND u.institutionName = :institutionname")
		List<Muser> findByRoleNameAndInstitutionName(@Param("rolename") String roleName, @Param("institutionname") String institutionName);
    
	   @Query("SELECT COUNT(u) FROM Muser u WHERE u.role.roleName = :rolename  AND u.institutionName = :institutionname")
	    Long countByRoleNameandInstitutionName(@Param("rolename") String roleName, @Param("institutionname") String institutionName);

	    @Query("SELECT COUNT(u) FROM Muser u WHERE u.role.roleName = :rolename")
	    Long countByRoleName(@Param("rolename") String roleName);
	    
	    @Query("SELECT isActive FROM Muser u WHERE u.role.roleName = :rolename  AND u.institutionName = :institutionname")
	    Boolean getactiveResultByInstitutionName(@Param("rolename") String roleName, @Param("institutionname") String institutionName);

//	    @Query("SELECT u.email FROM Muser u WHERE u.institutionName = :institutionname AND LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
//	    List<String> findEmailsByEmailContainingIgnoreCase(@Param("email") String email,@Param("institutionname") String institution);

	    @Query("SELECT new com.knowledgeVista.Batch.SearchDto(u.userId, u.username, 'EMAIL') " +
	    	       "FROM Muser u " +
	    	       "WHERE u.institutionName = :institutionName " +
	    	       "AND LOWER(u.username) LIKE LOWER(CONCAT('%', :email, '%'))")
	    	List<SearchDto> findEmailsAsSearchDto(@Param("email") String email, @Param("institutionName") String institutionName);

	    
	    @Query("SELECT DISTINCT u2.email " +
	            "FROM Muser u1 " +
	            "JOIN u1.allotedCourses c " +
	            "JOIN c.users u2 " +
	            "WHERE u1.email = :email " +
	            "AND u2.email LIKE %:query%")
	     List<String> findEmailsInAllotedCoursesByUserEmail(@Param("email") String email, @Param("query") String query);

	    @Query("SELECT new com.knowledgeVista.User.MuserDto(u2.userId, u2.username, u2.email, u2.phone, u2.isActive, u2.dob, u2.skills) " +
	            "FROM Muser u1 " +
	            "JOIN u1.allotedCourses c " +
	            "JOIN c.users u2 " +
	            "WHERE u1.email = :email " +
	            "GROUP BY u2.userId, u2.username, u2.email, u2.phone, u2.isActive, u2.dob, u2.skills,u2.institutionName")
	     List<MuserDto> findStudentsOfTrainer(@Param("email") String email);
	    
	    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
	            "FROM Muser u " +
	            "JOIN u.allotedCourses c " +
	            "WHERE u.userId = :userId AND c.courseId = :courseId")
	     boolean existsByTrainerIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
	    
	    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
	            "FROM Muser u " +
	            "JOIN u.courses c " +
	            "WHERE u.userId = :userId AND c.courseId = :courseId")
	     boolean existsByuserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
	    
	    
	    @Query("""
	    	    SELECT new com.knowledgeVista.User.UserStats(
	    	        m.userId, 
	    	        m.username,
	    	        m.profile,
	    	        COALESCE(SUM(CASE WHEN c.amount = 0 THEN 1 ELSE 0 END), 0) AS freeCourses,
	    	        COALESCE(SUM(CASE WHEN c.amount > 0 THEN 1 ELSE 0 END), 0) AS paidCourses,
	    	        COALESCE(SUM(CASE 
	    	            WHEN c.users IS NOT EMPTY THEN (SELECT COUNT(u.id) FROM c.users u)
	    	            ELSE 0 
	    	        END), 0) AS totalStudents
	    	    )
	    	    FROM Muser m
	    	    LEFT JOIN m.allotedCourses c
	    	    WHERE m.role.roleName = 'TRAINER'
	    	      AND m.institutionName = :institutionName
	    	    GROUP BY m.userId, m.username, m.profile
	    	""")
	    	List<UserStats> findTrainerStatsByInstitutionName(String institutionName);
	    
	    @Query("""
	    	    SELECT new com.knowledgeVista.User.UserStats(
	    	        m.userId, 
	    	        m.username,
	    	        m.profile,
	    	        COALESCE(SUM(CASE WHEN c.amount = 0 THEN 1 ELSE 0 END), 0) AS freeCourses,
	    	        COALESCE(SUM(CASE WHEN c.amount > 0 THEN 1 ELSE 0 END), 0) AS paidCourses
	    	        
	    	    )
	    	    FROM Muser m
	    	    LEFT JOIN m.courses c
	    	    WHERE m.role.roleName = 'USER'
	    	      AND m.institutionName = :institutionName
	    	    GROUP BY m.userId, m.username, m.profile
	    	""")
	    	List<UserStats> findStudentStatsByInstitutionName(String institutionName);

	    @Query("SELECT u FROM Muser u WHERE u.role.roleId = ?1")
		  Optional<Muser> findByroleid(Long roleId);
	    
	    @Query("SELECT u FROM Muser u WHERE u.role.roleId = ?1")
		  List<Muser> findByroleidSAS(Long roleId);
	    
	    @Query("SELECT new com.knowledgeVista.Migration.MuserMigrationDto(cd.userId, cd.username, cd.psw, cd.email, cd.phone, cd.isActive, cd.dob, cd.skills, cd.institutionName, cd.profile, cd.countryCode, cd.lastactive, cd.inactiveDescription, cd.role) FROM Muser cd WHERE cd.institutionName = :institutionName")
	    List<MuserMigrationDto> findAllByInstitutionNameDto(@Param("institutionName") String institutionName);

	    @Query("SELECT c.userId, c.username " +
	    	       "FROM Muser c " +
	    	       "WHERE (:username IS NOT NULL AND :username <> '' AND LOWER(c.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
	    	       "AND (:institutionName IS NOT NULL AND :institutionName <> '' AND LOWER(c.institutionName) LIKE LOWER(CONCAT('%', :institutionName, '%'))) " +
	    	       "AND c.role.roleName = 'TRAINER'")
	    	List<Object[]> searchTrainerIddAndTrainerNameByInstitution(
	    	    @Param("username") String username,
	    	    @Param("institutionName") String institutionName);
	    	
	    	   @Query("SELECT u FROM Muser u WHERE u.role.roleName ='TRAINER' AND u.userId=?1")
	 		  Optional<Muser> findtrainerByid(Long userid);

}






