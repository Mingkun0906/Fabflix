#!/bin/bash

# Get MySQL installation path
MYSQL_BASE=$(brew --prefix mysql)
PLUGIN_DIR="$MYSQL_BASE/lib/plugin"

echo "MySQL base directory: $MYSQL_BASE"
echo "Plugin directory: $PLUGIN_DIR"

# Create plugin directory if it doesn't exist
sudo mkdir -p "$PLUGIN_DIR"

# Compile the UDFs
make clean || true
make

# Copy the libraries to plugin directory
sudo cp libed*.so "$PLUGIN_DIR/"

# Set correct permissions
sudo chown -R _mysql:_mysql "$PLUGIN_DIR"
sudo chmod 755 "$PLUGIN_DIR"
sudo chmod 644 "$PLUGIN_DIR"/*.so

# Restart MySQL
brew services restart mysql

# Wait for MySQL to start
sleep 5

# Install the functions - create the SQL statements directly
mysql -u root -p << EOF
USE mysql;
DROP FUNCTION IF EXISTS ed;
CREATE FUNCTION ed RETURNS INTEGER SONAME 'libed.so';
DROP FUNCTION IF EXISTS edth;
CREATE FUNCTION edth RETURNS INTEGER SONAME 'libedth.so';
DROP FUNCTION IF EXISTS edrec;
CREATE FUNCTION edrec RETURNS INTEGER SONAME 'libedrec.so';
EOF

# Test the installation
mysql -u root -p -e "USE mysql; SELECT edth('hello', 'helo', 1) as test_result;"