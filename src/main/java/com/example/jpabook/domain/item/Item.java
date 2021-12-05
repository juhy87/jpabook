package com.example.jpabook.domain.item;

import com.example.jpabook.domain.Category;
import com.example.jpabook.exception.NotEnoughSotckException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="dtype")
@Getter
@Setter
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categorys = new ArrayList<>();

    /**
     * 재고 증가
     * @param quantity
     */
    public void addStock(int quantity){
        this.stockQuantity += quantity;
        
    }

    /**
     * 재고 감소
     * @param quantity
     */
    public void removeStock(int quantity){
        int restStock = this.stockQuantity - quantity;
        if(restStock < 0){
            throw new NotEnoughSotckException("need more stock");
        }
        this.stockQuantity = restStock;
    }
}
