package homes.banzzokee.global.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  FAILED(BAD_REQUEST, "실패했습니다."),
  USER_NOT_FOUND(NOT_FOUND, "사용자를 찾을 수 없습니다."),
  USER_ALREADY_WITHDRAWN(BAD_REQUEST, "이미 탈퇴한 사용자입니다."),
  PASSWORD_UNMATCHED(BAD_REQUEST, "패스워드가 일치하지 않습니다."),
  ORIGIN_PASSWORD_EQUALS_NEW_PASSWORD(BAD_REQUEST, "기존 패스워드와 새로운 패스워드가 같습니다."),
  CONFIRM_PASSWORD_UNMATCHED(BAD_REQUEST, "재확인 패스워드가 다릅니다."),
  CAN_FOLLOW_ONLY_SHELTER_USER(BAD_REQUEST, "보호소를 등록한 사용자만 팔로우할 수 있습니다."),
  CAN_NOT_FOLLOW_SELF(BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다."),
  FAIL_TO_UPLOAD_FILE(INTERNAL_SERVER_ERROR, "이미지 업로드에 실패하였습니다."),
  ARGUMENT_NOT_VALID(BAD_REQUEST, "잘못된 입력입니다."),
  JSON_EOF_ERROR(BAD_REQUEST, "잘못된 JSON 데이터입니다."),
  HTTP_MESSAGE_NOT_READABLE(BAD_REQUEST, "HTTP 메시지를 읽을 수 없습니다."),
  INTERNAL_ERROR(INTERNAL_SERVER_ERROR, "처리되지 않은 에러가 발생했습니다."),
  MIME_TYPE_INVALID(INTERNAL_SERVER_ERROR, "MIME TYPE을 찾을 수 없습니다."),
  EMAIL_UNMATCHED(BAD_REQUEST, "이메일이 일치하지 않습니다."),
  EMAIL_CODE_INVALID(BAD_REQUEST, "인증 코드가 유효하지 않습니다."),
  USER_ALREADY_REGISTER_SHELTER(BAD_REQUEST, "이미 보호소를 등록한 사용자입니다."),
  ADOPTION_NOT_FOUND(NOT_FOUND, "게시글을 찾을 수 없습니다"),
  // chat & room
  ROOM_NOT_FOUND(NOT_FOUND, "채팅방을 찾을 수 없습니다."),
  SHELTER_NOT_FOUND(NOT_FOUND, "보호소를 찾을 수 없습니다."),
  SHELTER_ALREADY_VERIFIED(BAD_REQUEST, "이미 승인된 보호소입니다."),
  NOT_VERIFIED_SHELTER_EXISTS(BAD_REQUEST, "승인되지 않은 보호소가 존재합니다."),
  NO_AUTHORIZED(FORBIDDEN, "권한이 없는 유저입니다."),
  EMAIL_DUPLICATED(CONFLICT, "중복되는 이메일이 존재합니다."),
  NICKNAME_DUPLICATED(BAD_REQUEST, "중복되는 닉네임이 존재합니다."),
  EMAIL_NOT_FOUND(NOT_FOUND, "이메일을 찾을 수 없습니다."),
  TOKEN_REQUIRED(UNAUTHORIZED, "토큰이 필요합니다."),
  REFRESH_TOKEN_EXPIRED(UNAUTHORIZED, "다시 로그인 해주세요."),
  INVALID_TOKEN(UNAUTHORIZED, "유효하지 않는 토큰입니다."),
  ACCESS_TOKEN_EXPIRED(UNAUTHORIZED, "만료된 토큰입니다. 토큰을 재발급 해주세요."),

  ;

  private final HttpStatus httpStatus;
  private final String message;
}
