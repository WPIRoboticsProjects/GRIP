/// <reference path="../typings/index.d.ts" />
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { createStore } from 'redux';
import { Provider } from 'react-redux';
import { App } from './components/app';
import { counterApp } from './reducers';
function configureStore() {
    const store = 
    /* tslint:disable */
    createStore(counterApp, window['__REDUX_DEVTOOLS_EXTENSION__'] &&
        window['__REDUX_DEVTOOLS_EXTENSION__']());
    /* tslint:enable */
    if (module.hot) {
        module.hot.accept('./reducers', () => {
            const nextRootReducer = require('./reducers').counterApp;
            store.replaceReducer(nextRootReducer);
        });
    }
    return store;
}
const store = configureStore();
class Main extends React.Component {
    render() {
        return (<Provider store={store}>
        <App />
      </Provider>);
    }
}
ReactDOM.render(<Main />, document.getElementById('app'));
