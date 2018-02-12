import cluster from "./cluster";
import {reducer as reduxFormReducer} from "redux-form";
import account from "./account";
import workspaces from "./workspaces";
import {combineReducers} from "redux";

export default combineReducers({
    form: reduxFormReducer,
    account,
    cluster,
    workspaces
});