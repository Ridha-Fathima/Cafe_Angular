package com.inn.cafe.cafe.POJO;

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import lombok.Data;


@NamedQuery(name = "Bill.getBills", query = "select b from Bill b order by b.id desc")
@NamedQuery(name = "Bill.getBillByUsername", query = "select b from Bill b where b.createdBy=:username order by b.id desc")

@Table(name = "bill")
@Data
@Entity
@DynamicInsert
@DynamicUpdate
public class Bill implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String uuid;
    private String name;
    private String email;
    private String contactNumber;
    private String paymentMethod;
    private Integer total;
    @Column(name = "product_detail", columnDefinition = "json")
    private String productDetail;
    private String createdBy;
}