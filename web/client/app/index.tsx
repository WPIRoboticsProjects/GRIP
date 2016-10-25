/// <reference path="../typings/index.d.ts" />

import * as React from 'react';
import * as ReactDOM from 'react-dom';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import {Store, createStore, applyMiddleware, compose} from 'redux';
import thunk from 'redux-thunk';
import {Provider} from 'react-redux';
import {App} from './components/app';
import {counterApp} from './reducers';
import * as injectTapEventPluginExport from 'react-tap-event-plugin';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import darkBaseTheme from 'material-ui/styles/baseThemes/darkBaseTheme';
import {MuiTheme} from 'material-ui/styles';
import {fetchOperations} from './actions';

// needed for onTouchTap
// http://stackoverflow.com/a/34015469/988941
const injectTapEventPlugin: () => void = (injectTapEventPluginExport as any).default;
injectTapEventPlugin();


declare const require: (name: String) => any;

interface IHotModule {
  hot?: { accept: (path: string, callback: () => void) => void };
}

declare const module: IHotModule;

/* tslint:disable */
const composeEnhancers = window['__REDUX_DEVTOOLS_EXTENSION_COMPOSE__'] || compose;
/* tslint:enable */

function configureStore(): Store {
  const store: Store =
    createStore(
      counterApp,
      composeEnhancers(
        applyMiddleware(
          thunk
      )));

  if (module.hot) {
    module.hot.accept('./reducers', () => {
      const nextRootReducer: any = require('./reducers').counterApp;
      store.replaceReducer(nextRootReducer);
    });
  }

  return store;
}

const store: Store = configureStore();
const multiTheme: MuiTheme = getMuiTheme(darkBaseTheme);

store.dispatch(fetchOperations());


class Main extends React.Component<{}, {}> {
  public render(): React.ReactElement<MuiThemeProvider> {
    return (
      <MuiThemeProvider muiTheme={multiTheme}>
        <Provider store={store}>
          <App />
        </Provider>
      </MuiThemeProvider>);
  }
}

ReactDOM.render(<Main />, document.getElementById('app'));
