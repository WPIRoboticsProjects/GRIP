/// <reference path="../../typings/index.d.ts" />

import * as React from 'react';

interface ICounterProps {
  index: number;
  value: number;
  onIncrement: () => void;
  onDecrement: () => void;
}

const COLORS: string[] = ['blue', 'green', 'red'];

export class Counter extends React.Component<ICounterProps, {}> {
  public render(): React.ReactElement<{}> {
    const style: {} = {
      color: COLORS[this.props.index % COLORS.length],
    };

    const {index, value, onIncrement, onDecrement}: any = this.props;

    return (
      <div>
        <p style={style}>Counters {index + 1}: {value}</p>
        <button onClick={onIncrement}>Increment</button>
        <button onClick={onDecrement}>Decrement</button>
      </div>
    );
  }
}
