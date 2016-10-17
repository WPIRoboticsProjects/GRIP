/// <reference path="../../typings/index.d.ts" />

import * as React from 'react';
import { connect } from 'react-redux';

import {incrementCounter, decrementCounter, addCounter, createOperationStep} from '../actions';
import { CounterList } from './counter_list';
import {OperationDescription} from 'grip-swagger';
import {GripToolbar} from './GripToolbar';
import {OperationList} from './operation_list';

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

    const operation: OperationDescription = {
      name: 'An operation',
    };
    const operation2: OperationDescription = {
      name: 'A second op',
    };
    return (
      <div>
        <GripToolbar/>
        <OperationList operationDescriptions={[operation, operation2]}
                       createOperation={(name: string) => dispatch(createOperationStep(name))}
        />
        <CounterList counters={counters}
                     increment={(index: number) => dispatch(incrementCounter(index))}
                     decrement={(index: number) => dispatch(decrementCounter(index))}
        />

        <button onClick={() => dispatch(addCounter())}>Add Counter</button>
      </div>
    );
  }
}
