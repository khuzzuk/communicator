import React, {Component} from 'react';
import './App.css';
import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";
import ChannelComponent from "./chat/ChannelComponent";

class App extends Component {
    state = {
        messages: []
    };
    socket;

    componentDidMount() {
        this.socket = new WebSocket('ws://localhost:8080/ws/channel');
        this.socket.addEventListener('message', event => {
            const message = JSON.parse(event.data);
            this.setState({messages: [...this.state.messages, message]})
        });
    }

    login = () => {
        if (this.state.name.length > 3) {
            this.setState({
                user: {
                    login: this.state.name
                },
                error: null
            });
        } else {
            this.setState({error: 'invalid user name'})
        }
    };

    sendMessage = event => {
        if (event.key === 'Enter' && this.state.messageText && this.state.messageText.length > 0) {
            this.socket.send(JSON.stringify({
                author: this.state.user,
                time: new Date(),
                text: this.state.messageText
            }));
            this.setState({messageText: ''})
        }
    };

    render() {
        if (!this.state.user) {
            return <div>
                <TextField label={'login'}
                           variant={'outlined'}
                           value={this.state.name}
                           onChange={event => this.setState({name: event.target.value})}/>
                {
                    this.state.error && <label>{this.state.error}</label>
                }
                <Button onClick={this.login}>Login</Button>
            </div>
        }

        return <ChannelComponent socket={this.socket} user={this.state.user} messages={this.state.messages}/>;
    }
}

export default App;
