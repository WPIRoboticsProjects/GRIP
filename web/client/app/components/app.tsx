/// <reference path="../../typings/index.d.ts" />

import * as React from 'react';
import { connect } from 'react-redux';
import * as Immutable from 'immutable';

import {incrementCounter, decrementCounter, addCounter, createOperationStep} from '../actions';
import { CounterList } from './counter_list';
import {OperationDescription} from 'grip-swagger';
import {GripToolbar} from './GripToolbar';
import {OperationList} from './operation_list';
import {Grid} from 'react-flexbox-grid';
import {Row} from 'react-flexbox-grid';
import {Col} from 'react-flexbox-grid';

export interface IOperationData {
  isFetching: boolean;
  didInvalidate: boolean;
  operations: OperationDescription[];
}

interface IAppState {
  counters: Immutable.List<number>;
  operationData: IOperationData;
}

interface IAppProps {
  dispatch?: (func: any) => void;
  counters?: Immutable.List<number>;
  operationData?: IOperationData;
}

function select(
  state: IAppState): IAppState {
  return Object.assign({}, state, {});
}

@connect(select)
export class App extends React.Component<IAppProps, {}> {
  public render(): React.ReactElement<{}> {
    const { dispatch, counters, operationData }: any = this.props;

    // const operation: OperationDescription = {
    //   name: 'An operation',
    //   summary: 'The summary',
    // };
    // const operation2: OperationDescription = {
    //   name: 'A second op',
    //   summary: 'The summary',
    // };
    return (
      <div>
        <GripToolbar/>
        <Grid>
          <Row>
            <Col xs={0} sm={8}/>
            <Col xs={12} sm={4}>
              <OperationList operationData={operationData}
                             createOperation={(name: string) => dispatch(createOperationStep(name))}
              />
            </Col>
          </Row>
        </Grid>
        <CounterList counters={counters}
                     increment={(index: number) => dispatch(incrementCounter(index))}
                     decrement={(index: number) => dispatch(decrementCounter(index))}
        />
        <button onClick={() => dispatch(addCounter())}>Add Counter</button>
      </div>
    );
  }
}
