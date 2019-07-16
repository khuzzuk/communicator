import React, {Component} from 'react';
import './App.css';

class App extends Component {
    state = {
        messages: []
    };

    componentDidMount() {
        const socket = new WebSocket('ws://localhost:8080/ws/channel');
        socket.addEventListener('message', event => {
            const message = JSON.parse(event.data);
            this.setState({messages: [...this.state.messages, message]})
        });
    }

    render() {
        const {messages} = this.state;

        return (
            <div>
                <div>Messages</div>
                <li>
                    {messages.map(message => <ul>{message.text}</ul>)}
                </li>
                <input aria-label={'message'}/>
            </div>
        );
    }
}

export default App;
