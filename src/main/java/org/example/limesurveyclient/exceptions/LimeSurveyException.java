package org.example.limesurveyclient.exceptions;

public class LimeSurveyException extends RuntimeException {
    public LimeSurveyException(String message) { super(message); }
    public LimeSurveyException(String message, Throwable cause) { super(message, cause); }
}
