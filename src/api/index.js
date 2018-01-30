const BASE_URL = "http://afaf4ca43f2b711e790d2066697211af-656732979.us-west-2.elb.amazonaws.com:8080";

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
            Authorization: "Bearer " + token
        }
    });
}

export function cluster() {
    return fetch(BASE_URL + "/clusters").then(response => response.json());
}