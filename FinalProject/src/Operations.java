
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author casper
 */
public class Operations {
    
     private Connection con = null;
    
   private Statement statement = null; //sql sorgularını çalıştırmak için
   
   private PreparedStatement preparedStatement = null;

    public Operations() {
        
        String url = "jdbc:mysql://" + Database.host + ":" + Database.port +  "/" + Database.db_ismi + "?UseUnicode=truecharaccterEncoding=utf8";

    try {
            con = DriverManager.getConnection(url,Database.kullanici_adi,Database.parola);
            System.out.println("Bağlantı başarılı...");
        } catch (SQLException ex) {

            System.out.println("Bağlantı başarısız...");
        }
    }
   
   
    
    
    public void addParent(String username, String password, String child_username){
        
         try {
             con.setAutoCommit(false);
             
        // Insert into users table
        String queryUser = "INSERT INTO users (username, password, role) VALUES (?, ?, 'parent')";
        PreparedStatement pstUser = con.prepareStatement(queryUser, Statement.RETURN_GENERATED_KEYS);
        pstUser.setString(1, username);
        pstUser.setString(2, password);
        pstUser.executeUpdate();
        
        
       // Get user ID
        ResultSet rs = pstUser.getGeneratedKeys();
        int userId = 0;
        if (rs.next()) {
            userId = rs.getInt(1);
        }
        
        // Insert into parents table
        String queryParent = "INSERT INTO parents (user_id, username) VALUES (?, ?)";
        PreparedStatement pstParent = con.prepareStatement(queryParent);
        pstParent.setInt(1, userId);
        pstParent.setString(2, username);
        pstParent.executeUpdate();
        

        // Update student record to set parent_id based on child's username
        String queryUpdateChild = "UPDATE students s " +
                                "JOIN users u ON s.user_id = u.id " +
                                "SET s.parent_id = ? " +
                                "WHERE u.username = ? AND u.role = 'student'";
        PreparedStatement pstUpdateChild = con.prepareStatement(queryUpdateChild);
        pstUpdateChild.setInt(1, userId);
        pstUpdateChild.setString(2, child_username);
        int rowsAffected = pstUpdateChild.executeUpdate();

        if (rowsAffected == 0) {
            throw new SQLException("Child username not found or not a student.");
        }

        con.commit(); 
        
        JOptionPane.showMessageDialog(null, "Parent registration successful!");

    } 
             
          catch (SQLException ex) {
             Logger.getLogger(Operations.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
    
    
   
    
    public void addTeacher(String username, String password){
        
         try { 
             con.setAutoCommit(false);
             
        // Insert into users table
        String queryUser = "INSERT INTO users (username, password, role) VALUES (?, ?, 'teacher')";
        PreparedStatement pstUser = con.prepareStatement(queryUser, Statement.RETURN_GENERATED_KEYS);
        pstUser.setString(1, username);
        pstUser.setString(2, password);
        pstUser.executeUpdate();
        
        // Get the generated user ID
        ResultSet rsUser = pstUser.getGeneratedKeys();
        int userId = 0;
        if (rsUser.next()) {
            userId = rsUser.getInt(1);
        }
        
        // Insert into teachers table
        String queryTeacher = "INSERT INTO teachers (user_id, username) VALUES (?, ?)";
        PreparedStatement pstTeacher = con.prepareStatement(queryTeacher);
        pstTeacher.setInt(1, userId);
        pstTeacher.setString(2, username);
        pstTeacher.executeUpdate();

        con.commit(); 
        
        
        JOptionPane.showMessageDialog(null, "Teacher registration successful!");
             
         } catch (SQLException ex) {
             Logger.getLogger(Operations.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
    
    
    
    
    public void addStudent(String username, String password,String math, String english, String physics){
         try {
        
        con.setAutoCommit(false); // Enable transaction

        // Insert into users table
        String queryUser = "INSERT INTO users (username, password, role) VALUES (?, ?, 'student')";
        preparedStatement = con.prepareStatement(queryUser, Statement.RETURN_GENERATED_KEYS);
        
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        
        preparedStatement.executeUpdate();

        
        // Get user ID
        ResultSet rs = preparedStatement.getGeneratedKeys();
        
        int userId = 0;
        if (rs.next()) {
            userId = rs.getInt(1);
        }

        // Insert into students table
        String queryStudent = "INSERT INTO students (user_id) VALUES (?)";
        preparedStatement = con.prepareStatement(queryStudent, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setInt(1, userId);
        preparedStatement.executeUpdate();

        // Get the generated student ID
        ResultSet rsStudent = preparedStatement.getGeneratedKeys();
        int studentId = 0;
        if (rsStudent.next()) {
            studentId = rsStudent.getInt(1);
        }

        // Insert target numbers into targets table
        insertTarget(studentId, "math", Integer.parseInt(math));
        insertTarget(studentId, "english", Integer.parseInt(english));
        insertTarget(studentId, "physics", Integer.parseInt(physics));

        con.commit(); // Commit transaction
        

        
        JOptionPane.showMessageDialog(null, "Registration successful!");
        
        

    } catch (SQLException ex) {
        ex.printStackTrace();
    }
 
    }
    
    // at registering step
    private void insertTarget(int studentId, String courseName, int target) throws SQLException {
    // Get the course ID
    String queryCourse = "SELECT id FROM courses WHERE name = ?";
    PreparedStatement pstCourse = con.prepareStatement(queryCourse);
    pstCourse.setString(1, courseName);
    ResultSet rs = pstCourse.executeQuery();

    int courseId = 0;
    if (rs.next()) {
        courseId = rs.getInt("id");
    }

    // Insert into targets table
    String queryTarget = "INSERT INTO targets (student_id, course_id, target, remaining) VALUES (?, ?, ?, ?)";
    PreparedStatement pstTarget = con.prepareStatement(queryTarget);
    pstTarget.setInt(1, studentId);
    pstTarget.setInt(2, courseId);
    pstTarget.setInt(3, target);
    pstTarget.setInt(4, target); //remaining starts equal to target
    pstTarget.executeUpdate();
}
    
    
    // for show at student page and parent page's loadStudentProgress method
    public List<Target> getTargetsForStudent(int studentId) {
        List<Target> targets = new ArrayList<>();
        try {
            
            String query = "SELECT c.id, c.name, t.target, t.remaining FROM targets t " +
                         "JOIN courses c ON t.course_id = c.id WHERE t.student_id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, studentId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int courseId = rs.getInt("id");
                String courseName = rs.getString("name");
                int targetNumber = rs.getInt("target");
                int remaining = rs.getInt("remaining");

                Target target = new Target(courseId, courseName, targetNumber, remaining);
                targets.add(target);
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return targets;
    }
    
    
 
    // for change from student page
    public void changeTarget(int studentId, String courseName, int newTarget) {
    try {
        // Start a transaction
        con.setAutoCommit(false);

        // Update the target value
        String updateTargetSql = "UPDATE targets t JOIN courses c ON t.course_id = c.id " +
                                 "SET t.target = ? WHERE t.student_id = ? AND c.name = ?";
        PreparedStatement pst = con.prepareStatement(updateTargetSql);
        pst.setInt(1, newTarget);
        pst.setInt(2, studentId);
        pst.setString(3, courseName);
        pst.executeUpdate();

        // Recalculate the remaining value
        String updateRemainingSql = "UPDATE targets t JOIN courses c ON t.course_id = c.id " +
                                    "SET t.remaining = ? WHERE t.student_id = ? AND c.name = ?";
        PreparedStatement pstRemaining = con.prepareStatement(updateRemainingSql);
        pstRemaining.setInt(1, newTarget);        
        pstRemaining.setInt(2, studentId);
        pstRemaining.setString(3, courseName);
        pstRemaining.executeUpdate();

        // Commit the transaction
        con.commit();
        con.setAutoCommit(true);
        
    } catch (SQLException ex) {
        ex.printStackTrace();
        try {
            // Rollback in case of an error
            con.rollback();
            con.setAutoCommit(true);
        } catch (SQLException rollbackEx) {
            rollbackEx.printStackTrace();
        }
    }
}

    
    
    public ResultSet loginAsStudent(String username, String password) {
        try {
            
            String query = "SELECT s.id FROM students s JOIN users u ON s.user_id = u.id WHERE u.username = ? AND u.password = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            return rs;
                         
        }
        catch (Exception e) {
            e.printStackTrace();
        }
   
        return null;  
        
    }
    
    public ResultSet loginAsParent(String username, String password){
        //childusername added
        String query = "SELECT p.id as parent_id, s.id as student_id, u2.username as child_username " +
               "FROM parents p " +
               "JOIN students s ON p.user_id = s.parent_id " +
               "JOIN users u ON p.user_id = u.id " +
               "JOIN users u2 ON s.user_id = u2.id " +
               "WHERE u.username = ? AND u.password = ?";
        
        PreparedStatement pst;
         try {
             pst = con.prepareStatement(query);
             pst.setString(1, username);
             pst.setString(2, password);
             ResultSet rs = pst.executeQuery();
             
             return rs;
         } catch (SQLException ex) {
             Logger.getLogger(Operations.class.getName()).log(Level.SEVERE, null, ex);
         }
       return null;
    }
    
    
    public ResultSet loginAsTeacher(String username, String password) {
        String query = "SELECT t.id FROM teachers t JOIN users u ON t.user_id = u.id WHERE u.username = ? AND u.password = ?";
        try {
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, password);
            return pst.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(Operations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    
    
    public void uploadQuestion(int studentId, int courseId, String imagePath) {
    String query = "INSERT INTO questions (student_id, course_id, image_path) VALUES (?, ?, ?)";

    try {
        PreparedStatement pst = con.prepareStatement(query);
                
        pst.setInt(1, studentId);
        pst.setInt(2, courseId);
        pst.setString(3, imagePath);
        pst.executeUpdate();
    } catch (SQLException ex) {
        Logger.getLogger(Operations.class.getName()).log(Level.SEVERE, null, ex);
    }
}
    
    
    
    public void uploadSolution(int questionId, String solutionPath) {
    String query = "UPDATE questions SET solution_path = ? WHERE id = ?";
    try (PreparedStatement pst = con.prepareStatement(query)) {
        pst.setString(1, solutionPath);
        pst.setInt(2, questionId);
        pst.executeUpdate();
    } catch (SQLException ex) {
        Logger.getLogger(Operations.class.getName()).log(Level.SEVERE, null, ex);
    }
}
    
    
    public List<Question> getQuestions() {
    List<Question> questions = new ArrayList<>();
    String query = "SELECT q.id, q.image_path, s.id AS student_id, u.username AS student_username, c.name AS course_name " +
                   "FROM questions q " +
                   "JOIN students s ON q.student_id = s.id " +
                   "JOIN users u ON s.user_id = u.id " +
                   "JOIN courses c ON q.course_id = c.id";
    try (PreparedStatement pst = con.prepareStatement(query);
         ResultSet rs = pst.executeQuery()) {
        while (rs.next()) {
            int questionId = rs.getInt("id");
            String imagePath = rs.getString("image_path");
            int studentId = rs.getInt("student_id");
            String studentUsername = rs.getString("student_username");
            String courseName = rs.getString("course_name");
            questions.add(new Question(questionId, studentId, studentUsername, imagePath, courseName));
        }
    } catch (SQLException ex) {
        Logger.getLogger(Operations.class.getName()).log(Level.SEVERE, null, ex);
    }
    return questions;
}
    
    
    
    public int getCourseIdByName(String courseName) {
    String query = "SELECT id FROM courses WHERE name = ?";

   
    try {
         PreparedStatement pst = con.prepareStatement(query);
        pst.setString(1, courseName);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return rs.getInt("id");
        }
    } catch (SQLException ex) {
        Logger.getLogger(Operations.class.getName()).log(Level.SEVERE, null, ex);
    }

    return -1; // Return -1 if course is not found
}
    
    
    // when user changed the target value or solved the question, remaining value should change
    public void updateRemainingQuestions(int studentId, String courseName, int newRemaining) {
    String query = "UPDATE targets t JOIN courses c ON t.course_id = c.id " +
                   "SET t.remaining = ? WHERE t.student_id = ? AND c.name = ?";
    try (PreparedStatement pst = con.prepareStatement(query)) {
        pst.setInt(1, newRemaining);
        pst.setInt(2, studentId);
        pst.setString(3, courseName);
        pst.executeUpdate();
    } catch (SQLException ex) {
        Logger.getLogger(Operations.class.getName()).log(Level.SEVERE, null, ex);
    }
}
    
    
    
 }   

