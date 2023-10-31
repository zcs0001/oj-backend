package com.tree.backend.judge.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Builder 注解是 Lombok 提供的，用于自动生成一个建造者模式相关的方法。通过它，可以方便地使用链式调用的方式构建对象，提高代码的可读性和易用性。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {

    private List<String> inputList;

    private String code;

    private String language;
}
