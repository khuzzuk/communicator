import React, {Component} from "react";
import withStyles from "@material-ui/core/styles/withStyles";
import List from "@material-ui/core/List";
import ListItem from "@material-ui/core/ListItem";
import {ListItemAvatar} from "@material-ui/core";
import Avatar from "@material-ui/core/Avatar";
import ListItemText from "@material-ui/core/ListItemText";
import TextField from "@material-ui/core/TextField";
import * as ReactDOM from "react-dom";

const style = {
    channelContainer: {
        height: 900,
    },
    messagesContainer: {
        height: 800,
        overflow: 'auto',
    }
};

class ChannelComponent extends Component {
    state = {};

    sendMessage = event => {
        if (event.key === 'Enter' && this.state.messageText && this.state.messageText.length > 0) {
            this.props.socket.send(JSON.stringify({
                author: this.props.user,
                time: new Date(),
                text: this.state.messageText
            }));
            this.setState({messageText: ''})
        }
    };

    componentDidUpdate() {
        ReactDOM.findDOMNode(this.refs.messageList).scrollTop = 1000000;
    }

    render() {
        const {classes} = this.props;

        return <div className={classes.channelContainer}>
            <List className={classes.messagesContainer} ref={'messageList'}>
                {
                    this.props.messages.map(message =>
                        <ListItem>
                            <ListItemAvatar>
                                <Avatar>
                                    {message.author && message.author.login && message.author.login.charAt(0)}
                                </Avatar>
                            </ListItemAvatar>
                            <ListItemText primary={message.author && message.author.login}
                                          secondary={message.text}/>
                        </ListItem>
                    )
                }
            </List>
            <div>
                <TextField onChange={event => this.setState({messageText: event.target.value})}
                           value={this.state.messageText}
                           onKeyPress={this.sendMessage}
                           variant={'outlined'}/>
            </div>
        </div>
    }
}

export default withStyles(style)(ChannelComponent);