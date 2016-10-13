/// <reference path="../../typings/index.d.ts" />
import * as React from 'react';
const COLORS = ['blue', 'green', 'red'];
export class Counter extends React.Component {
    render() {
        const style = {
            color: COLORS[this.props.index % COLORS.length],
        };
        const { index, value, onIncrement, onDecrement } = this.props;
        return (<div>
        <p style={style}>Counters {index + 1}: {value}</p>
        <button onClick={onIncrement}>Increment</button>
        <button onClick={onDecrement}>Decrement</button>
      </div>);
    }
}
