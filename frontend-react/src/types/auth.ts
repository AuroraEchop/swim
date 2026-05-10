export interface LoginUser {
  id: number
  username: string
  realName: string
  roleCode: string
}

export interface LoginResponse {
  loginToken: string
  tokenType: string
  user: LoginUser
}

export interface CurrentUser extends LoginUser {
  permissions: string[]
}
