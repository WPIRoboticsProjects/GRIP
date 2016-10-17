/// <reference path="../typings/index.d.ts" />
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import { createStore } from 'redux';
import { Provider } from 'react-redux';
import { App } from './components/app';
import { counterApp } from './reducers';
import * as injectTapEventPluginExport from 'react-tap-event-plugin';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import darkBaseTheme from 'material-ui/styles/baseThemes/darkBaseTheme';
// needed for onTouchTap
// http://stackoverflow.com/a/34015469/988941
const injectTapEventPlugin = injectTapEventPluginExport.default;
injectTapEventPlugin();
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
const multiTheme = getMuiTheme(darkBaseTheme);
class Main extends React.Component {
    render() {
        return (<MuiThemeProvider muiTheme={multiTheme}>
        <Provider store={store}>
          <App />
        </Provider>
      </MuiThemeProvider>);
    }
}
ReactDOM.render(<Main />, document.getElementById('app'));
