/// <reference path="../../typings/index.d.ts" />
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
import * as React from 'react';
import { connect } from 'react-redux';
import { incrementCounter, decrementCounter, addCounter, createOperationStep } from '../actions';
import { CounterList } from './counter_list';
import { GripToolbar } from './GripToolbar';
import { OperationList } from './operation_list';
function select(state) {
    return {
        counters: state.counters,
        operations: state.operations,
    };
}
export let App = class App extends React.Component {
    render() {
        const { dispatch, counters } = this.props;
        const operation = {
            name: 'An operation',
        };
        const operation2 = {
            name: 'A second op',
        };
        return (<div>
        <GripToolbar />
        <OperationList operationDescriptions={[operation, operation2]} createOperation={(name) => dispatch(createOperationStep(name))}/>
        <CounterList counters={counters} increment={(index) => dispatch(incrementCounter(index))} decrement={(index) => dispatch(decrementCounter(index))}/>

        <button onClick={() => dispatch(addCounter())}>Add Counter</button>
      </div>);
    }
};
App = __decorate([
    connect(select)
], App);
