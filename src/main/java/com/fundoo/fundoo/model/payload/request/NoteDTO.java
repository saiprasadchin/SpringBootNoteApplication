package com.fundoo.fundoo.model.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
@AllArgsConstructor
public class NoteDTO {
    private String title;
    private String description;
}
