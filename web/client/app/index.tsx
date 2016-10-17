/// <reference path="../typings/index.d.ts" />

import * as React from 'react';
import * as ReactDOM from 'react-dom';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import {Store, createStore} from 'redux';
import {Provider} from 'react-redux';
import {App} from './components/app';
import {counterApp} from './reducers';
import * as injectTapEventPluginExport from 'react-tap-event-plugin';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import darkBaseTheme from 'material-ui/styles/baseThemes/darkBaseTheme';
import {MuiTheme} from 'material-ui/styles';

// needed for onTouchTap
// http://stackoverflow.com/a/34015469/988941
const injectTapEventPlugin: () => void = (injectTapEventPluginExport as any).default;
injectTapEventPlugin();


declare const require: (name: String) => any;

interface IHotModule {
  hot?: { accept: (path: string, callback: () => void) => void };
}

declare const module: IHotModule;

function configureStore(): Store {
  const store: Store =
    /* tslint:disable */
    createStore(
      counterApp,
      window['__REDUX_DEVTOOLS_EXTENSION__'] &&
      window['__REDUX_DEVTOOLS_EXTENSION__']());
  /* tslint:enable */

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
