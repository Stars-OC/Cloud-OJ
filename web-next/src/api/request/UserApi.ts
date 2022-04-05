import { JudgeResult, Overview, PagedData, User, UserInfo } from "@/api/type"
import { buildHeaders, returnError } from "@/api/utils"
import ApiPath from "./ApiPath"
import axios from "axios"

const UserApi = {
  /**
   * 获取所有用户
   * @param page 页数
   * @param limit 每页数量
   * @param params 搜索参数
   * @param userInfo {@link UserInfo}
   */
  getAll(
    page: number,
    limit: number,
    params: any,
    userInfo: UserInfo
  ): Promise<PagedData<User>> {
    return new Promise<PagedData<User>>((resolve, reject) => {
      axios({
        url: ApiPath.USER_ADMIN,
        method: "GET",
        headers: buildHeaders(userInfo),
        params: {
          page,
          limit,
          userId: params.userId,
          name: params.name
        }
      })
        .then((res) => {
          resolve(res.status === 200 ? res.data : { data: [], count: 0 })
        })
        .catch((error) => {
          reject(returnError(error))
        })
    })
  },

  getProfile(userId: string): Promise<User> {
    return new Promise<User>((resolve, reject) => {
      axios({
        url: ApiPath.PROFILE,
        method: "GET",
        params: {
          userId
        }
      })
        .then((res) => {
          resolve(res.data as User)
        })
        .catch((error) => {
          reject(returnError(error))
        })
    })
  },

  getOverview(userId: string, year: number) {
    return new Promise<Overview>((resolve, reject) => {
      axios({
        url: ApiPath.OVERVIEW,
        method: "GET",
        params: {
          userId,
          year
        }
      })
        .then((res) => {
          resolve(res.data as Overview)
        })
        .catch((error) => {
          reject(returnError(error))
        })
    })
  },

  getSolutions(
    page: number,
    limit: number,
    userInfo: UserInfo,
    searchParam?: { problemId?: number; title?: string }
  ) {
    return new Promise<PagedData<JudgeResult>>((resolve, reject) => {
      axios({
        url: ApiPath.HISTORY,
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          token: userInfo.token!,
          userId: userInfo.userId!
        },
        params: {
          page,
          limit,
          problemId: searchParam?.problemId,
          title: searchParam?.title
        }
      })
        .then((res) => {
          if (res.status === 200) {
            resolve(res.data as PagedData<JudgeResult>)
          } else {
            resolve({ data: [], count: 0 })
          }
        })
        .catch((error) => {
          reject(returnError(error))
        })
    })
  }
}

export default UserApi
