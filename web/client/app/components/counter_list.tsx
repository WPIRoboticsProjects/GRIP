/// <reference path="../../typings/index.d.ts" />

import * as React from 'react';

import { Counter } from './counter';

interface ICounterListProps {
  counters: number[];
  increment: (index: number) => void;
  decrement: (index: number) => void;
}

export class CounterList extends React.Component<ICounterListProps, {}> {
  public render(): React.ReactElement<{}> {
    const {increment, decrement}: any = this.props;

    return (<ul>
      {this.props.counters.map((value: number, index: number) =>
        <li key={index}>
          <Counter
            index={index}
            onIncrement={() => increment(index)}
            onDecrement={() => decrement(index)}
            value={value}
          />
        </li>
      )}
    </ul>);
  }
}
