export interface Login {
    username: String
    password: String
}

export interface Auth {
    token: String | Boolean
    error: String | Boolean
    loading: Boolean
    loggingIn: Boolean
}

export interface StoreState {
    auth: Auth
}