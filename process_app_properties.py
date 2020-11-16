import os
import sys
if os.environ.get('LOCAL_ONLY'): sys.exit(0)
import boto3
import botocore
from botocore.exceptions import ClientError

# initialize / constants
ssm = boto3.client("ssm")
sm = boto3.client('secretsmanager')
app_name = None
operations = ["plan", "pull", "push"]
usage = ("\nusage: This utility needs two arguments (three if operation is PULL ). \n"
         "python script.py <operation> <application-properties-file-name>|<app-name> \n"
         "supported operations: plan | pull | push \n"
         "plan - displays what they script will do when push command is executed\n"
         "pull - reads all ssm/sm parameters related to application and writes them into a file app.properties\n"
         "push - writes the latest application properties into sm/ssm\n\n"
         "if the operation is plan OR push, provide the application properties file as an argument\n"
         "if the operation is pull, provide the application name as argument(using this app name prefix secrets were "
         "stored in aws)\n"
         "Also pull needs an additional argument, a file name towrite application properties\n"
         )


def get_ssm_param(param_name):
    try:
        parameter = ssm.get_parameter(Name=param_name, WithDecryption=True)
    except botocore.exceptions.ClientError:
        parameter = None
    return parameter


def create_ssm_param(prop, prop_wt_prefix, param_value, Type='String'):
    parameter = get_ssm_param(prop_wt_prefix)

    if parameter is None or param_value != parameter.get("Parameter").get("Value"):
        if operation == "plan":
            if parameter is None:
                print("parameter:" + prop + " will be created in ssm.")
            else:
                print("parameter:" + prop + " will be updated in ssm.")

        else:
            response = ssm.put_parameter(Name=prop_wt_prefix,
                                         Value=param_value,
                                         Type=Type,
                                         Overwrite=True,
                                         Tier='Standard'
                                         )
            if parameter is None:
                label = "created"
            else:
                label = "updated"

            if response["ResponseMetadata"]["HTTPStatusCode"] == 200:
                print("parameter:" + prop + " " + label + " in ssm successfully")
            else:
                print("parameter:" + prop + " update failed")
    else:
        if operation == "plan":
            print("No action will be performed for parameter:" + prop)
        else:
            print("No action performed for parameter:" + prop)


def delete_ssm_params(props, Type):
    # print(props)
    response = ssm.get_parameters_by_path(Path=app_name_prefix + "ssm/",
                                          Recursive=True,
                                          WithDecryption=False,
                                          )

    for parameter in response["Parameters"]:
        # print(parameter)
        param_type = parameter["Type"]
        if parameter["Name"] not in props and param_type == Type:
            if operation == "plan":
                if "secret" in prop_file:
                    print("parameter:" + parameter["Name"].replace(app_name_prefix, "") + " will be deleted.")
                else:
                    print("parameter:" + parameter["Name"].replace(app_name_prefix + "ssm/", "") + " will be deleted.")
            else:
                response = ssm.delete_parameter(Name=parameter["Name"])
                if response["ResponseMetadata"]["HTTPStatusCode"] == 200:
                    if "secret" in prop_file:
                        print("parameter:" + parameter["Name"].replace(app_name_prefix, "") +
                              " deleted successfully")
                    else:
                        print("parameter:" + parameter["Name"].replace(app_name_prefix + "ssm/",
                                                                       "") + " deleted successfully")


def process_props():
    latest_props = []
    with open(prop_file) as application_prop:
        for prop in application_prop:
            # print(prop)
            if prop.startswith("#") or not prop.strip() or prop.startswith("app_name="):
                # print(prop)
                continue
            param_name, param_value = prop.partition("=")[::2]
            if "ssm/" not in name:
                prop_wt_prefix = app_name_prefix + "ssm/" + param_name
            else:
                prop_wt_prefix = app_name_prefix + param_name

            latest_props.append(prop_wt_prefix)
            # if value.startswith('\"') and value.endswith('\"'):
            #     value = value[1:-1]
            #     print("hello")
            create_ssm_param(param_name, prop_wt_prefix, param_value.strip())

    # delete the parameters deleted from application properties file
    delete_ssm_params(latest_props, Type="String")


def pull_ssm_props():
    response = ssm.get_parameters_by_path(Path="/" + app_name + "/",
                                          Recursive=True,
                                          WithDecryption=True,
                                          )

    f = open("application_write.properties", "w")
    for parameter in response["Parameters"]:
        f.write(parameter["Name"] + "=" + parameter["Value"] + "\n")
    f.close()


def get_sm_param(param_name):
    try:
        parameter = sm.get_secret_value(SecretId=param_name)
    except botocore.exceptions.ClientError:
        parameter = None
    return parameter


def create_sm_param(param_name, param_value):
    if "sm/" not in param_name:
        parameter = get_sm_param(app_name_prefix + "sm/" + param_name)
    else:
        parameter = get_sm_param(app_name_prefix + param_name)
    if parameter is None or param_value != parameter["SecretString"]:
        if operation == "plan":
            if parameter is None:
                print("parameter:" + param_name + " will be created in SM.")
            elif param_value != parameter["SecretString"]:
                print("parameter:" + param_name + " will be updated in SM.")
        else:
            if parameter is None:
                response = sm.create_secret(Name=app_name_prefix + param_name,
                                            SecretString=param_value,
                                            )
                if response["ResponseMetadata"]["HTTPStatusCode"] == 200:
                    print("parameter:" + param_name + " created in SM successfully")
                else:
                    print("parameter:" + param_name + " update failed in SM")

            elif param_value != parameter["SecretString"]:
                response = sm.update_secret(SecretId=param_name, SecretString=param_value)
                if response["ResponseMetadata"]["HTTPStatusCode"] == 200:
                    print("parameter:" + param_name + " updated in SM successfully")
                else:
                    print("parameter:" + param_name + " update failed in SM")
    else:
        if operation == "plan":
            print("No action will be performed for parameter:" + param_name)
        else:
            print("No action performed for parameter:" + param_name)


def delete_sm_params(latest_sm_props):
    # pull sm props list
    aws_sm_props = []
    response = sm.list_secrets()
    # print(response)
    # filter by appname and write a list
    for secret in response["SecretList"]:
        if secret["Name"].startswith(app_name_prefix + "sm/"):
            aws_sm_props.append(secret["Name"])

    for sm_prop in aws_sm_props:
        if sm_prop not in latest_sm_props:
            if operation == "plan":
                print("parameter:" + sm_prop.replace(app_name_prefix, "") + " will be deleted.")
            else:
                response = sm.delete_secret(SecretId=sm_prop, )
                if "DeletionDate" in response:
                    print("parameter:" + sm_prop.replace(app_name_prefix, "") + " deleted successfully")


def process_secret_props():
    print("processing secrets")
    latest_ssm_props = []
    latest_sm_props = []
    with open(prop_file) as application_prop:
        for prop in application_prop:
            if prop.startswith("#") or not prop.strip() or prop.startswith("app_name="):
                continue
            param_name, param_value = prop.partition("=")[::2]
            prop_wt_prefix = app_name_prefix + param_name
            if "ssm/" in param_name:
                latest_ssm_props.append(app_name_prefix + param_name)
                create_ssm_param(param_name, prop_wt_prefix, param_value.strip(), Type="SecureString")
            elif "sm/" in param_name:
                latest_sm_props.append(app_name_prefix + param_name)
                create_sm_param(param_name, param_value.strip())

    # delete the parameters deleted from application properties file
    delete_ssm_params(latest_ssm_props, Type="SecureString")
    delete_sm_params(latest_sm_props)


def pull_props():
    # pull ssm props
    response = ssm.get_parameters_by_path(Path=app_name_prefix + "ssm/",
                                          Recursive=True,
                                          WithDecryption=True,
                                          )

    f = open(output_file, "w")
    # write ssm props
    for parameter in response["Parameters"]:
        # print(parameter)
        param_name = parameter["Name"].replace(app_name_prefix + "ssm/", "")
        f.write(param_name + "=" + parameter["Value"] + "\n")

    # pull sm props
    response = sm.list_secrets()
    # print(response)
    # filter by appname and write sm props
    for secret in response["SecretList"]:
        if secret["Name"].startswith("/" + app_name + "/"):
            secret_value = get_sm_param(secret["Name"])
            param_name = secret["Name"].replace(app_name_prefix + "sm/", "")
            f.write(param_name + "=" + secret_value["SecretString"] + "\n")
    f.close()


if __name__ == "__main__":
    prop_file = None
    operation = None

    # validate args
    if len(sys.argv) < 3:
        print(usage)
        sys.exit(2)
    else:
        operation = sys.argv[1]
        if operation not in operations:
            print(usage)
            sys.exit(2)
        if operation == "pull":
            if len(sys.argv) < 4:
                print(usage)
                sys.exit(2)
            app_name = sys.argv[2]
            output_file = sys.argv[3]
            app_name_prefix = "/" + app_name + "/"
            pull_props()
        else:
            prop_file = sys.argv[2]
            # validate if app_name property exist in the application.properties
            try:
                with open(prop_file) as app_prop:
                    for param in app_prop:
                        if param.startswith("app_name="):
                            name, value = param.partition("=")[::2]
                            app_name = value.strip()
                    if app_name is None:
                        print("ERROR: app_name must be specified in the application properties file")
                        sys.exit(2)
                app_name_prefix = "/" + app_name + "/"
                if "secret" in prop_file:
                    process_secret_props()
                else:
                    process_props()
            except IOError:
                print(prop_file + " file not found")
