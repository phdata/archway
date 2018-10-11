import * as React from 'react';
import { Card, Col, Row } from 'antd';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { Login } from '../../types/Login';
import { login } from './actions';
import { Colors } from '../../components';
import { isLoggingIn, loginError } from './selectors';
import { Dispatch } from 'redux';
import { LoginForm } from './components';
import logo from '../../images/logo_black.png';

interface Props {
    onSubmit: (payload: Login) => void;
    loggingIn: boolean;
    error: string;
}

class LoginPage extends React.PureComponent<Props> {
    public render() {
    return (
  <Row
    type="flex"
    justify="center"
    align="middle"
    style={{ minHeight: '100%', backgroundColor: Colors.LightGray.string() }}>
    <Col span={24}>
      <Row type="flex" justify="center">
        <Col span={10} style={{ textAlign: 'center' }}>
          <img src={logo} style={{ maxHeight: 250 }} alt="logo" />
        </Col>
      </Row>
      <Row type="flex" justify="center">
        <Col span={12} xxl={6}>
          <Card bodyStyle={{ textAlign: 'center' }}>
            <h1 className="Login-title">Please log in</h1>
            <LoginForm {...this.props} />
            {this.props.error && <div className="Login-error">{this.props.error}</div>}
          </Card>
        </Col>
      </Row>
    </Col>
  </Row>
    );
  }
}

const mapStateToProps = () =>
  createStructuredSelector({
    loggingIn: isLoggingIn(),
    error: loginError(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  onSubmit: (payload: Login) => dispatch(login(payload)),
});

export default connect(mapStateToProps, mapDispatchToProps)(LoginPage);
