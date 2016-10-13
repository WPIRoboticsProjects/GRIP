/// <reference path="../../typings/index.d.ts" />
import * as React from 'react';
import { Counter } from './counter';
export class CounterList extends React.Component {
    render() {
        const { increment, decrement } = this.props;
        return (<ul>
      {this.props.counters.map((value, index) => <li key={index}>
          <Counter index={index} onIncrement={() => increment(index)} onDecrement={() => decrement(index)} value={value}/>
        </li>)}
    </ul>);
    }
}
