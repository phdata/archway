export const LOGIN_REQUEST = 'LOGIN_REQUEST';
export const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
export const LOGIN_FAILURE = 'LOGIN_FAILURE';

export function login({username, password}) {
    return {
        type: LOGIN_REQUEST,
        username,
        password
    }
}

export function tokenRetrieved(token) {
    return {
        type: LOGIN_SUCCESS,
        token
    }
}