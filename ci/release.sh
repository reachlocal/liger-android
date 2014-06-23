#/bin/bash -e

# Abort checks
if [ "$TRAVIS" != "true" ]; then
	echo "Fatal: not running in Travis. Aborting."
	exit 1
fi

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
	echo "Travis thinks this is a pull request; no release."
	exit 0
fi

if [ "$TRAVIS_BRANCH" != "stable" ]; then
	echo "Commit is not to a release branch; no release."
	exit 0
fi

echo "Releasing a new version of liger-android."

# Debug output
git remote -v
git status
git log --decorate --graph -n 3

# Add github deploy key
chmod 600 .travis/deploy_key.pem
ssh-add .travis/deploy_key.pem

# Push to github
git tag $tag
git push origin --tags

# Release library to bintray
version="0.1.0"

# Upload build artifact
# Expected response in form of {"message":"success"}
upload=`curl \
	-u$bintray_key \
	-H "X-Bintray-Package:liger-android" \
	-H "X-Bintray-Version:$version" \
	-T "liger-android-library/build/outputs/aar/liger-android-library-$version.aar" \
	https://api.bintray.com/content/reachlocal/liger-android/liger-android-library-$version.aar`

# Check response
echo "Upload response:" $upload
result=`echo $upload | jq ".message"`

if [ $result != "\"success\"" ] ; then
	echo "Release upload to bintray failed."
	exit 1
fi

# Publish newversion
# Expected reponse in form of {"files":1}
publish=`curl -X POST \
	-u$bintray_key \
	https://api.bintray.com/content/reachlocal/liger-android/liger-android/$version/publish`

# Check response
echo "Publish response:" $publish
result=`echo $publish | jq ".files"`

if [ $result != "1" ] ; then
	echo "Release publish to bintray failed."
	exit 2
fi

"Release successful!"
