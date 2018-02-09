const BASE_URL = "https://private-a63a5-heimdali.apiary-mock.com";

export function login(username, password) {
    return fetch(BASE_URL + "/account/token", {
        headers: {
            "Authorization": "Basic " + btoa(username + ":" + password),
        }
    }).then(response => response.json());
}

export function logout() {

}

export function workspace(token) {
    return fetch(BASE_URL + "/account/workspace", {
        headers: {
            "Authorization": "Bearer " + token
        }
    }).then(response => response.json());
}

export function requestWorkspace(token) {
    return fetch(BASE_URL + "/account/workspace", {
        method: 'POST',
        headers: {
            "Authorization": "Bearer " + token
        }
    });
}

export function cluster() {
    return fetch(BASE_URL + "/clusters", {})
        .then(response => response.json());
}

export function profile(token) {
    return fetch(BASE_URL + "/account/profile", {
        headers: {
            "Authorization": "Bearer " + token
        }
    }).then(response => response.json());
}