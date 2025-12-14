package com.dbt.chatease.Utils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Result", description = "Unified API response wrapper")
public class Result {
    @Schema(description = "Indicates if the request was successful", example = "true")
    private Boolean success;

    @Schema(description = "Error message if request failed", example = "Invalid parameters")
    private String errorMsg;

    @Schema(description = "Returned data, can be any type", example = "{ \"id\": 1, \"name\": \"John\" }")
    private Object data;

    @Schema(description = "Total number of items for paginated responses", example = "100")
    private Long total;

    public static Result ok() {
        return new Result(true, null, null, null);
    }

    public static Result ok(Object data) {
        return new Result(true, null, data, null);
    }

    public static Result ok(List<?> data, Long total) {
        return new Result(true, null, data, total);
    }

    public static Result ok(Object data, Long total) {
        return new Result(true, null, data, total);
    }

    public static Result fail(String errorMsg) {
        return new Result(false, errorMsg, null, null);
    }
}
