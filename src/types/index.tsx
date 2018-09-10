export interface Login {
    email: String
    password: String
}

export interface Auth {
    token: String
    error: String
    loading?: Boolean
    loggingIn: Boolean
}

export interface StoreState {
    auth: Auth
}