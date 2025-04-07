/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author casper
 */
public class Question {
    private int questionId;
    private int studentId;
    private String studentUsername;
    private String imagePath;
    private String courseName;

    public Question(int questionId, int studentId, String studentUsername, String imagePath, String courseName) {
        this.questionId = questionId;
        this.studentId = studentId;
        this.studentUsername = studentUsername;
        this.imagePath = imagePath;
        this.courseName = courseName;
    }

    public int getQuestionId() {
        return questionId;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getCourseName() {
        return courseName;
    }
}

