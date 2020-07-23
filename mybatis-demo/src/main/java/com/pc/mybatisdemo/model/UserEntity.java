package com.pc.mybatisdemo.model;

import lombok.*;
import java.io.Serializable;

/**
 *
 * @author dongxie
 * @date 17:17 2020-05-08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserEntity implements Serializable {

    private Long id;
    private String userName;

}