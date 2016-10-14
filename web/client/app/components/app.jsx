/// <reference path="../../typings/index.d.ts" />
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
import * as React from 'react';
import { connect } from 'react-redux';
import { incrementCounter, decrementCounter, addCounter } from '../actions';
import { CounterList } from './counter_list';
function select(state) {
    return {
        counters: state.counters,
        operations: state.operations,
    };
}
export let App = class App extends React.Component {
    render() {
        const { dispatch, counters } = this.props;
        return (<div>
        <h1>GRIP Webapp</h1>
        <CounterList counters={counters} increment={(index) => dispatch(incrementCounter(index))} decrement={(index) => dispatch(decrementCounter(index))}/>

        <button onClick={() => dispatch(addCounter())}>Add Counter</button>
      </div>);
    }
};
App = __decorate([
    connect(select)
], App);
