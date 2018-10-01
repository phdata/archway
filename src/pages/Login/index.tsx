import { Button, Col, Form, Icon, Input, Row, Card } from 'antd';
import { FormikProps, withFormik, InjectedFormikProps } from 'formik';
import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { Login } from '../../types/Login';
import { login } from './actions';
import logo from './logo_black.png';
import { isLoggingIn, loginError } from './selectors';
import { Dispatch } from 'redux';
import Colors from '../../components/Colors';

interface Props {
  onSubmit: (payload: Login) => void;
  loggingIn: boolean;
  error: string;
}

class LoginForm extends React.PureComponent<InjectedFormikProps<Props, Login>> {
  public render() {
    const { handleSubmit, handleChange, values: { username, password } } = this.props;
    return (
      <Form
        onSubmit={handleSubmit}
        className="LoginForm"
        layout="vertical">
        <Form.Item>
          <Input
            name="username"
            onChange={handleChange}
            size="large"
            value={username}
            prefix={<Icon type="user" />}
            placeholder="username" />
        </Form.Item>
        <Form.Item>
          <Input
            name="password"
            onChange={handleChange}
            size="large"
            value={password}
            prefix={<Icon type="lock" />}
            placeholder="password"
            type="password" />
        </Form.Item>
        <Form.Item>
          <Button
            type="primary"
            size="large"
            htmlType="submit">
            Log In
    </Button>
        </Form.Item>
      </Form>
    );
  }
}

const LoginFormRender = withFormik<Props, Login>({
  handleSubmit: (values, { props }) => props.onSubmit(values),
})(LoginForm);

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
            <LoginFormRender {...this.props} />
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
