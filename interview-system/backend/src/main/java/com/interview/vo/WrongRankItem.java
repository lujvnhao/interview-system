package com.interview.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WrongRankItem {
    private Long id;
    private String question;
    private Integer wrongCount;
}
