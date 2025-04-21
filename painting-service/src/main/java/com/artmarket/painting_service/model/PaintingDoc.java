package com.artmarket.painting_service.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;


@Document(indexName = "painting_index")
@Setting(settingPath = "/elasticsearch-settings.json")
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaintingDoc {
    @Id
    Long id;

    @Field(type = FieldType.Text, analyzer = "english_analyzer")
    String title;

    @Field(type = FieldType.Text, analyzer = "english_analyzer")
    String description;

    @Field(type = FieldType.Text, analyzer = "english_analyzer")
    String author;

}
