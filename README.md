# AWS Lambda: S3 Key Management in Java

This repository contains the Java implementation of an AWS Lambda function used to **download and upload keys to Amazon S3**, with secure authentication and best practices for managing sensitive configuration.

## 🚀 Features

- 🔐 **Secure Authentication**  
  Authenticates requests using environment variables or IAM role permissions to avoid hardcoding credentials.

- 📥 **Download Keys from S3**  
  Fetches keys or other secured files from a specified S3 bucket and path.

- 📤 **Upload Keys to S3**  
  Uploads new or updated key files into the target S3 bucket, optionally with metadata or encryption settings.

- 🛡️ **Environment Variable Support**  
  Uses environment variables to securely store and access:
  - Bucket names
  - Key paths
  - Authentication tokens or secrets
  - Any other configuration

## 🧪 Use Cases

- Centralized key rotation system
- Secure distribution of credentials across environments
- Integration with CI/CD pipelines for secret deployment

## 🛠 Technologies Used

- Java (8+)
- AWS Lambda
- AWS SDK for Java
- Amazon S3
- Environment variable-based configuration

## 🧾 Example Environment Variables

| Variable Name     | Purpose                      |
|-------------------|------------------------------|
| `SOURCE_BUCKET`   | S3 bucket to download keys from |
| `TARGET_BUCKET`   | S3 bucket to upload keys to   |
| `ACCESS_KEY`      | (Optional) AWS access key     |
| `SECRET_KEY`      | (Optional) AWS secret key     |
| `KEY_PREFIX`      | Path prefix for locating keys in S3 |

> ⚠️ It's recommended to use **IAM roles** for authentication in production environments instead of hardcoding credentials.

## 🏗️ Deployment

You can deploy this function using:

- AWS Lambda Console
- AWS CLI
- AWS SAM
- Terraform / CDK (optional)

Make sure to set the required environment variables in your Lambda configuration.

## 📂 Project Structure

