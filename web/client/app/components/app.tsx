/// <reference path="../../typings/index.d.ts" />

import * as React from 'react';
import { connect } from 'react-redux';

import { incrementCounter, decrementCounter, addCounter } from '../actions';
import { CounterList } from './counter_list';
import {OperationDescription} from 'grip-swagger';
import {GripToolbar} from './GripToolbar';

interface IAppState {
  counters: number[];
  operations: OperationDescription[];
}

interface IAppProps {
  dispatch?: (func: any) => void;
  counters?: number[];
}

function select(
  state: {
    counters: number[],
    operations: OperationDescription[]
  }): IAppState {
  return {
    counters: state.counters,
    operations: state.operations,
  };
}

@connect(select)
export class App extends React.Component<IAppProps, {}> {
  public render(): React.ReactElement<{}> {
    const { dispatch, counters }: any = this.props;

    return (
      <div>
        <GripToolbar/>
        <h1>GRIP Webapp</h1>
        <CounterList counters={counters}
                     increment={(index: number) => dispatch(incrementCounter(index))}
                     decrement={(index: number) => dispatch(decrementCounter(index))}
        />

        <button onClick={() => dispatch(addCounter())}>Add Counter</button>
      </div>
    );
  }
}
