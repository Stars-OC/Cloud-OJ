import type { AxiosError } from "axios"
import { ErrorMessage, UserInfo } from "@/api/type"

function resolveError(error: any): ErrorMessage {
  const err = error as AxiosError
  console.log(err)
  if (err.response) {
    if (
      err.response.data &&
      err.response.headers["content-type"] === "application/json"
    ) {
      return ErrorMessage.from(err.response.data)
    } else {
      return new ErrorMessage(err.response.status, err.response.statusText)
    }
  } else if (err.request) {
    return new ErrorMessage(0, "请求失败")
  } else {
    return new ErrorMessage(-1, err.message)
  }
}

function buildHeaders(userInfo: UserInfo | null): any {
  if (userInfo == null) {
    return {
      "Content-Type": "application/json"
    }
  } else {
    return {
      "Content-Type": "application/json",
      token: userInfo.token
    }
  }
}

export { resolveError, buildHeaders }