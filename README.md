# Heimdali UI

Heimdali UI is a web application for managing resources.

##### What's Being Used?

* [React](http://facebook.github.io/react/) for managing the presentation logic of your application.
* [Redux](http://redux.js.org/) + [Redux-Immutable](https://github.com/gajus/redux-immutable/) + [Reselect](https://github.com/reduxjs/reselect/) for generating and managing your state model.
* [Redux-Saga](https://github.com/redux-saga/redux-saga/) for managing application side effects.
* [Antd](https://ant.design/) for ui elements such as sidebar, dropdown, card, etc.
* [Formik](https://github.com/jaredpalmer/formik/) and [Redux-Form](https://redux-form.com/) for handling forms efficiently.
* [Fuse](http://fusejs.io/) for fuzzy-search feature.
* [React-Csv](https://github.com/react-csv/react-csv/) for exporting data in csv format.
* [Lodash](https://lodash.com/) for using various utility functions.


## Getting Started
In order to get started developing, you'll need to do a few things first.

1. Install all of the `node_modules` required for the package. Depending on your computer's configuration, you may need to prefix this command with a `sudo`.

npm >= 5.0,       node >= 8.0
```
npm install
```

2. Make sure you are using correct API endpoint url. Replace the base url if needed.

```
window.config = {
  baseUrl: "http://master1.jotunn.io:8080" //%%BASE_URL%%"
};
```

3. Lastly, run the start command to get the project off the ground. This command will build your JS files using the Webpack `dev-server`.

```
npm start
```

4. Head over to [http://localhost:3000](http://localhost:3000) to see your app live!

5. Run tests.

```
npm run test
```
when you need to update snapshots, you need to add `-u` flag.
```
npm run test -u
```

6. Create a production-ready build.

```
npm run build
```

## File Structure

### public/

In this folder is a default `index.html` file for serving up the application. Fonts used by application also reside here.

##### images/

Folder containing image assets used in the application.

### src/

The client folder houses the client application for your project.  This is where your client-side Javascript components, logical code blocks and image assets live.

##### components/

Here reside the components that are used globally, such as ListCardToggle, Behavior and WorkspaceListItem.

##### containers/

Here we have containers, the components that are connected to redux. Each container has its own actions, reducers, sagas and selectors. Every routes of the app has their relevant containers. The sub-directory names indicate what route they are pointing. Some containers has subfolder named `components` that includes components used for that specific container.

##### models/

Here we define all the models used for the application, including Workspace and Cluster.

##### redux/

Global reducers and sagas of the application stays here.

##### service/

Here resides the code for making api calls.
