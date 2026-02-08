import {merge} from 'webpack-merge';
import HtmlWebpackPlugin from 'html-webpack-plugin';

import {webpackCommon} from './webpack.common.js';

export default merge(webpackCommon, {
  mode: 'development',
  devtool: 'inline-source-map',
  output: {
    // web workers public path
    // (generated at root for dev)
    workerPublicPath: '/'
  },
  entry: {
    viewer: './tests/pacs/viewer.js',
  },
  devServer: {
    port: 3000,
    open: '/tests/pacs/viewer.html',
    static: [
      {
        directory: './tests',
        publicPath: '/tests'
      }
    ],
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: './tests/pacs/viewer.html',
      filename: 'tests/pacs/viewer.html',
      scriptLoading: 'module',
      chunks: ['viewer'],
    }),
  ],
  module: {
    rules: [
      {
        test: /\.png/,
        type: 'asset/resource'
      },
      {
        test: /\.json/,
        type: 'asset/source'
      },
      {
        test: /\.dcm/,
        generator: {
          dataUrl: {
            encoding: 'base64',
            mimetype: 'application/dicom',
          },
        },
        type: 'asset/inline'
      },
      {
        test: /\.zip/,
        generator: {
          dataUrl: {
            encoding: 'base64',
            mimetype: 'application/zip',
          },
        },
        type: 'asset/inline'
      },
      {
        test: /DICOMDIR/,
        generator: {
          dataUrl: {
            encoding: 'base64',
            mimetype: 'application/dicom',
          },
        },
        type: 'asset/inline'
      }
    ]
  }
});
