const router = require('connected-react-router/immutable');
import { History } from 'history';
import { fromJS } from 'immutable';
import { applyMiddleware, compose, createStore, Store } from 'redux';
import createSagaMiddleware from 'redux-saga';
import SagaManager from './sagas';
import reducers from './reducers';

const store: (history: History) => Store<any> = (history) => {
  const composeEnhancers = (<any>window).__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;
  const sagaMiddleware = createSagaMiddleware();

  const enhancer = composeEnhancers(
    applyMiddleware(
      sagaMiddleware,
      router.routerMiddleware(history),
    ),
  );

  const result = createStore(
    router.connectRouter(history)(reducers),
    fromJS({}),
    enhancer,
  );

  SagaManager.startSagas(sagaMiddleware);

  return result;
}

export default store;