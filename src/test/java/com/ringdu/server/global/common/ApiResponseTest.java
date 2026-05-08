package com.ringdu.server.global.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void successWithData() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("요청이 성공했습니다.");
        assertThat(response.getData()).isEqualTo("ok");
    }

    @Test
    void failWithoutData() {
        ApiResponse<Void> response = ApiResponse.fail("error");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("error");
        assertThat(response.getData()).isNull();
    }
}
