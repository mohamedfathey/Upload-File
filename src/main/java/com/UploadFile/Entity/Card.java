package com.UploadFile.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "card")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;

    private String cardName;
    private String cardDetails;
    private String cardMediaURL; // تعديل الاسم ليكون متناسق مع تسمية المتغيرات الأخرى

    // Getters and Setters

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCardDetails() {
        return cardDetails;
    }

    public void setCardDetails(String cardDetails) {
        this.cardDetails = cardDetails;
    }

    public String getCardMediaURL() {
        return cardMediaURL;
    }

    public void setCardMediaURL(String cardMediaURL) {
        this.cardMediaURL = cardMediaURL;
    }
}
